// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.modules.cache.chroniclemap;

import com.google.common.cache.AbstractLoadingCache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.gerrit.server.cache.PersistentCache;
import com.google.gerrit.server.cache.PersistentCacheDef;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.MapEntry;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

public class ChronicleMapCacheImpl<K, V> extends AbstractLoadingCache<K, V>
    implements PersistentCache {

  private final ChronicleMapCacheConfig config;
  private final CacheLoader<K, V> loader;
  private final ChronicleMap<K, V> store;
  private final LongAdder hitCount = new LongAdder();
  private final LongAdder missCount = new LongAdder();
  private final LongAdder loadSuccessCount = new LongAdder();
  private final LongAdder loadExceptionCount = new LongAdder();
  private final LongAdder totalLoadTime = new LongAdder();
  private final LongAdder evictionCount = new LongAdder();

  @SuppressWarnings("unchecked")
  ChronicleMapCacheImpl(
      PersistentCacheDef<K, V> def,
      ChronicleMapCacheConfig config,
      CacheLoader<K, V> loader)
      throws IOException {
    this.config = config;
    this.loader = loader;
    final Class<K> keyClass = (Class<K>) def.keyType().getRawType();
    final Class<V> valueClass = (Class<V>) def.valueType().getRawType();

    final ChronicleMapBuilder<K, V> mapBuilder =
        ChronicleMap.of(keyClass, valueClass).name(def.name());

    if (!isWrapperType(keyClass)) {
      mapBuilder.averageKeySize(config.getAverageKeySize());
    }

    if (!isWrapperType(valueClass)) {
      mapBuilder.averageValueSize(config.getAverageValueSize());
    }

    mapBuilder.keyMarshaller(new ChronicleMapMarshallerAdapter<>(def.keySerializer()));
    mapBuilder.valueMarshaller(new ChronicleMapMarshallerAdapter<>(def.valueSerializer()));

    // TODO: limit is the max number of bytes, not entries
      mapBuilder.entries(config.getMaxEntries());

    if (config.getPersistedFile() == null || config.getLimit() < 0) {
      store = mapBuilder.create();
    } else {
      store = mapBuilder.createOrRecoverPersistedTo(config.getPersistedFile());
    }
  }

  @Override
  public V getIfPresent(Object objKey) {
    if (store.containsKey(objKey)) {
      hitCount.increment();
      return store.get(objKey);
    }
    missCount.increment();
    return null;
  }

  @Override
  public V get(K key) throws ExecutionException {

    if (store.containsKey(key)) {
      hitCount.increment();
      return store.get(key);
    }
    missCount.increment();
    if (loader != null) {
      V v = null;
      try {
        long start = System.nanoTime();
        v = loader.load(key);
        totalLoadTime.add(System.nanoTime() - start);
        loadSuccessCount.increment();
      } catch (Exception e) {
        loadExceptionCount.increment();
        throw new ExecutionException(String.format("Could not load value %s", key), e);
      }
      put(key, v);
      return v;
    }

    loadExceptionCount.increment();
    throw new UnsupportedOperationException(
        String.format("Could not load value for %s without any loader", key));
  }

  @Override
  public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
    if (store.containsKey(key)) {
      hitCount.increment();
      return store.get(key);
    }
    missCount.increment();
    V v = null;
    try {
      long start = System.nanoTime();
      v = valueLoader.call();
      totalLoadTime.add(System.nanoTime() - start);
      loadSuccessCount.increment();
    } catch (Exception e) {
      loadExceptionCount.increment();
      throw new ExecutionException(String.format("Could not load key %s", key), e);
    }
    put(key, v);
    return v;
  }

  @Override
  public void put(K key, V val) {
    store.put(key, val);
  }

  @Override
  public void invalidate(Object key) {
    evictionCount.increment();
    store.remove(key);
  }

  @Override
  public void invalidateAll() {
    evictionCount.add(store.size());
    store.forEachEntry(MapEntry::doRemove);
  }

  @Override
  public long size() {
    return store.size();
  }

  @Override
  public CacheStats stats() {
    return new CacheStats(
        hitCount.longValue(),
        missCount.longValue(),
        loadSuccessCount.longValue(),
        loadExceptionCount.longValue(),
        totalLoadTime.longValue(),
        evictionCount.longValue());
  }

  @Override
  public DiskStats diskStats() {
    return null;
  }

  public void close() {
    store.close();
  }

  private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

  public static boolean isWrapperType(Class<?> clazz) {
    return WRAPPER_TYPES.contains(clazz);
  }

  private static Set<Class<?>> getWrapperTypes() {
    Set<Class<?>> ret = new HashSet<>();
    ret.add(Boolean.class);
    ret.add(Character.class);
    ret.add(Byte.class);
    ret.add(Short.class);
    ret.add(Integer.class);
    ret.add(Long.class);
    ret.add(Float.class);
    ret.add(Double.class);
    ret.add(Void.class);
    return ret;
  }
}
