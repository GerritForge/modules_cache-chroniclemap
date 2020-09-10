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
import com.google.common.cache.CacheStats;
import com.google.gerrit.server.cache.PersistentCache;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class ChronicleMapCacheImpl<K, V> extends AbstractLoadingCache<K, V>
    implements PersistentCache {

  ChronicleMapCacheImpl() {}

  @Override
  public V getIfPresent(Object objKey) {
    return null;
  }

  @Override
  public V get(K key) throws ExecutionException {
    return null;
  }

  @Override
  public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
    return null;
  }

  @Override
  public void put(K key, V val) {}

  @Override
  public void invalidate(Object key) {}

  @Override
  public void invalidateAll() {}

  @Override
  public long size() {
    return 0L;
  }

  @Override
  public CacheStats stats() {
    return null;
  }

  @Override
  public DiskStats diskStats() {
    return null;
  }
}
