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
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.MapEntry;

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
      PersistentCacheDef<K, V> def, ChronicleMapCacheConfig config, CacheLoader<K, V> loader)
      throws IOException {
    this.config = config;
    this.loader = loader;
    final Class<K> keyClass = (Class<K>) def.keyType().getRawType();
    final Class<V> valueClass = (Class<V>) def.valueType().getRawType();

    final ChronicleMapBuilder<K, V> mapBuilder =
        ChronicleMap.of(keyClass, valueClass).name(def.name());

    // Chronicle-map does not allow to custom-serialize boxed primitives
    // such as Boolean, Integer, for which size is statically determined.
    // This means that even though a custom serializer was provided for a primitive
    // it cannot be used.
    // TODO: Should we log.warn when this is the case?
    if (!mapBuilder.constantlySizedKeys()) {
      mapBuilder.averageKeySize(config.getAverageKeySize());
      mapBuilder.keyMarshaller(new ChronicleMapMarshallerAdapter<>(def.keySerializer()));
    }

    if (!mapBuilder.constantlySizedValues()) {
      mapBuilder.averageValueSize(config.getAverageValueSize());
      mapBuilder.valueMarshaller(new ChronicleMapMarshallerAdapter<>(def.valueSerializer()));
    }

    // TODO: ChronicleMap must have "entries" configured, however cache definition
    //  has already the concept of diskLimit. How to reconcile the two when both
    //  are defined?
    //  Should we honour diskLimit, by computing entries as a function of (avgKeySize +
    // avgValueSize)
    mapBuilder.entries(config.getMaxEntries());

    if (config.getPersistedFile() == null || config.getDiskLimit() < 0) {
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
    store.remove(key);
  }

  @Override
  public void invalidateAll() {
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
    return new DiskStats(
        size(), config.getPersistedFile().length(), hitCount.longValue(), missCount.longValue());
  }

  public void close() {
    store.close();
  }
}
