// Copyright (C) 2012 The Android Open Source Project
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.registration.DynamicMap;
import com.google.gerrit.server.cache.CacheBackend;
import com.google.gerrit.server.cache.MemoryCacheFactory;
import com.google.gerrit.server.cache.PersistentCacheDef;
import com.google.gerrit.server.cache.PersistentCacheFactory;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.lib.Config;

@Singleton
class ChronicleMapCacheFactory implements PersistentCacheFactory {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final MemoryCacheFactory memCacheFactory;
  private final Config config;
  private final Path cacheDir;

  @Inject
  ChronicleMapCacheFactory(
      MemoryCacheFactory memCacheFactory,
      @GerritServerConfig Config cfg,
      SitePaths site,
      DynamicMap<Cache<?, ?>> cacheMap) {
    this.memCacheFactory = memCacheFactory;
    config = cfg;
    cacheDir = getCacheDir(site, cfg.getString("cache", null, "directory"));
  }

  private static Path getCacheDir(SitePaths site, String name) {
    if (name == null) {
      return null;
    }
    Path loc = site.resolve(name);
    if (!Files.exists(loc)) {
      try {
        Files.createDirectories(loc);
      } catch (IOException e) {
        logger.atWarning().log("Can't create disk cache: %s", loc.toAbsolutePath());
        return null;
      }
    }
    if (!Files.isWritable(loc)) {
      logger.atWarning().log("Can't write to disk cache: %s", loc.toAbsolutePath());
      return null;
    }
    logger.atInfo().log("Enabling disk cache %s", loc.toAbsolutePath());
    return loc;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <K, V> Cache<K, V> build(PersistentCacheDef<K, V> in, CacheBackend backend) {
    long limit = config.getLong("cache", in.configKey(), "diskLimit", in.diskLimit());

    if (cacheDir == null || limit <= 0) {
      return memCacheFactory.build(in, backend);
    }

    return new ChronicleMapCacheImpl<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> LoadingCache<K, V> build(
      PersistentCacheDef<K, V> in, CacheLoader<K, V> loader, CacheBackend backend) {
    long limit = config.getLong("cache", in.configKey(), "diskLimit", in.diskLimit());

    if (cacheDir == null || limit <= 0) {
      return memCacheFactory.build(in, loader, backend);
    }

    return new ChronicleMapCacheImpl<>();
  }

  @Override
  public void onStop(String plugin) {}
}
