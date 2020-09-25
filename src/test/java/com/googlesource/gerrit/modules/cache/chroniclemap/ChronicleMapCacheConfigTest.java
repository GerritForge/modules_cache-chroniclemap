// Copyright (C) 2015 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.modules.cache.chroniclemap.ChronicleMapCacheConfig.DEFAULT_AVG_KEY_SIZE;
import static com.googlesource.gerrit.modules.cache.chroniclemap.ChronicleMapCacheConfig.DEFAULT_AVG_VALUE_SIZE;
import static com.googlesource.gerrit.modules.cache.chroniclemap.ChronicleMapCacheConfig.DEFAULT_MAX_ENTRIES;

import com.google.gerrit.server.config.SitePaths;
import java.nio.file.Files;
import java.time.Duration;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChronicleMapCacheConfigTest {

  private final String cacheName = "foobar-cache";
  private final String cacheKey = "foobar-cache-key";
  private final long definitionDiskLimit = 100;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private SitePaths sitePaths;
  private StoredConfig gerritConfig;

  @Before
  public void setUp() throws Exception {
    sitePaths = new SitePaths(temporaryFolder.newFolder().toPath());
    Files.createDirectories(sitePaths.etc_dir);

    gerritConfig =
        new FileBasedConfig(
            sitePaths.resolve("etc").resolve("gerrit.config").toFile(), FS.DETECTED);
    gerritConfig.load();
  }

  @Test
  public void shouldProvidePersistedFileWhenCacheDirIsConfigured() throws Exception {
    final String directory = "cache-dir";
    gerritConfig.setString("cache", null, "directory", directory);
    gerritConfig.save();

    assertThat(
            configUnderTest(gerritConfig)
                .getPersistedFile()
                .toPath()
                .getParent()
                .toRealPath()
                .toString())
        .isEqualTo(sitePaths.resolve(directory).toRealPath().toString());
  }

  @Test
  public void shouldNotProvidePersistedFileWhenCacheDirIsNotConfigured() {
    assertThat(configUnderTest(gerritConfig).getPersistedFile()).isNull();
  }

  @Test
  public void shouldProvideConfiguredDiskLimitWhenDefined() throws Exception {
    long configuredDiskLimit = 50;
    gerritConfig.setLong("cache", cacheKey, "diskLimit", configuredDiskLimit);
    gerritConfig.save();

    assertThat(configUnderTest(gerritConfig).getDiskLimit()).isEqualTo(configuredDiskLimit);
  }

  @Test
  public void shouldProvideDefinitionDiskLimitWhenNotCofigured() {
    assertThat(configUnderTest(gerritConfig).getDiskLimit()).isEqualTo(definitionDiskLimit);
  }

  @Test
  public void shouldProvideConfiguredMaxEntriesWhenDefined() throws Exception {
    long maxEntries = 10;
    gerritConfig.setLong("cache", cacheKey, "maxEntries", maxEntries);
    gerritConfig.save();

    assertThat(configUnderTest(gerritConfig).getMaxEntries()).isEqualTo(maxEntries);
  }

  @Test
  public void shouldProvideDefaultMaxEntriesWhenNotConfigured() {
    assertThat(configUnderTest(gerritConfig).getMaxEntries()).isEqualTo(DEFAULT_MAX_ENTRIES);
  }

  @Test
  public void shouldProvideAverageKeySizeWhenConfigured() throws Exception {
    long averageKeySize = 5;
    gerritConfig.setLong("cache", cacheKey, "avgKeySize", averageKeySize);
    gerritConfig.save();

    assertThat(configUnderTest(gerritConfig).getAverageKeySize()).isEqualTo(averageKeySize);
  }

  @Test
  public void shouldProvideDefaultAverageKeySizeWhenNotConfigured() {
    assertThat(configUnderTest(gerritConfig).getAverageKeySize()).isEqualTo(DEFAULT_AVG_KEY_SIZE);
  }

  @Test
  public void shouldProvideAverageValueSizeWhenConfigured() throws Exception {
    long averageValueSize = 6;
    gerritConfig.setLong("cache", cacheKey, "avgValueSize", averageValueSize);
    gerritConfig.save();

    assertThat(configUnderTest(gerritConfig).getAverageValueSize()).isEqualTo(averageValueSize);
  }

  @Test
  public void shouldProvideDefaultAverageValueSizeWhenNotConfigured() {
    assertThat(configUnderTest(gerritConfig).getAverageValueSize())
        .isEqualTo(DEFAULT_AVG_VALUE_SIZE);
  }

  private ChronicleMapCacheConfig configUnderTest(StoredConfig gerritConfig) {
    return new ChronicleMapCacheConfig(
        gerritConfig,
        sitePaths,
        cacheName,
        cacheKey,
        definitionDiskLimit,
        Duration.ZERO,
        Duration.ZERO);
  }
}
