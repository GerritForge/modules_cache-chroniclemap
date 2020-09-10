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

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.lib.Config;

public class ChronicleMapCacheConfig {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();;
  private final File persistedFile;
  private final long diskLimit;
  private final long maxEntries;
  private final long averageKeySize;
  private final long averageValueSize;

  public static final long DEFAULT_MAX_ENTRIES = 1000;

  // TODO: Should find some sensible defaults, rather than 1 Byte
  public static final long DEFAULT_AVG_KEY_SIZE = 1;
  public static final long DEFAULT_AVG_VALUE_SIZE = 1;

  public interface Factory {
    ChronicleMapCacheConfig create(
        @Assisted("Name") String name,
        @Assisted("ConfigKey") String configKey,
        @Assisted("DiskLimit") long diskLimit);
  }

  @AssistedInject
  ChronicleMapCacheConfig(
      @GerritServerConfig Config cfg,
      SitePaths site,
      @Assisted("Name") String name,
      @Assisted("ConfigKey") String configKey,
      @Assisted("DiskLimit") long diskLimit) {
    final Path cacheDir = getCacheDir(site, cfg.getString("cache", null, "directory"));
    this.persistedFile =
        cacheDir != null ? cacheDir.resolve(String.format("%s.dat", name)).toFile() : null;
    this.diskLimit = cfg.getLong("cache", configKey, "diskLimit", diskLimit);

    this.maxEntries = cfg.getLong("cache", configKey, "maxEntries", DEFAULT_MAX_ENTRIES);
    this.averageKeySize = cfg.getLong("cache", configKey, "avgKeySize", DEFAULT_AVG_KEY_SIZE);
    this.averageValueSize = cfg.getLong("cache", configKey, "avgValueSize", DEFAULT_AVG_VALUE_SIZE);
  }

  public long getMaxEntries() {
    return maxEntries;
  }

  public File getPersistedFile() {
    return persistedFile;
  }

  public long getAverageKeySize() {
    return averageKeySize;
  }

  public long getAverageValueSize() {
    return averageValueSize;
  }

  public long getDiskLimit() {
    return diskLimit;
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
}
