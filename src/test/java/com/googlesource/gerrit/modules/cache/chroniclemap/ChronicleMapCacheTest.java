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
import static com.google.gerrit.testing.GerritJUnit.assertThrows;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.Weigher;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.server.cache.PersistentCacheDef;
import com.google.gerrit.server.cache.serialize.CacheSerializer;
import com.google.gerrit.server.cache.serialize.StringCacheSerializer;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChronicleMapCacheTest {

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
  public void getIfPresentShouldReturnNullWhenThereisNoCachedValue() throws Exception {
    assertThat(newCacheWithLoader(null).getIfPresent("foo")).isNull();
  }

  @Test
  public void getWithLoaderShouldPopulateTheCache() throws Exception {
    String cachedValue = UUID.randomUUID().toString();
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    assertThat(cache.get("foo", () -> cachedValue)).isEqualTo(cachedValue);
    assertThat(cache.get("foo")).isEqualTo(cachedValue);
  }

  @Test
  public void getShouldRetrieveTheValueViaTheLoader() throws Exception {
    String cachedValue = UUID.randomUUID().toString();
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader(cachedValue);

    assertThat(cache.get("foo")).isEqualTo(cachedValue);
  }

  @Test
  public void getShoudThrowWhenNoLoaderHasBeenProvided() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithoutLoader();

    UnsupportedOperationException thrown =
        assertThrows(UnsupportedOperationException.class, () -> cache.get("foo"));
    assertThat(thrown).hasMessageThat().contains("Could not load value");
  }

  @Test
  public void shouldIncreaseMissCountWhenValueIsNotInCache() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    cache.getIfPresent("foo");
    assertThat(cache.stats().hitCount()).isEqualTo(0);
    assertThat(cache.stats().missCount()).isEqualTo(1);
  }

  @Test
  public void shouldIncreaseHitCountWhenValueIsInCache() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    cache.put("foo", "bar");
    cache.getIfPresent("foo");

    assertThat(cache.stats().hitCount()).isEqualTo(1);
    assertThat(cache.stats().missCount()).isEqualTo(0);
  }

  @Test
  public void shouldIncreaseLoadSuccessCountWhenValueIsLoadedFromCacheDefinitionLoader()
      throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    cache.get("foo");

    assertThat(cache.stats().loadSuccessCount()).isEqualTo(1);
    assertThat(cache.stats().loadExceptionCount()).isEqualTo(0);
  }

  @Test
  public void valueShouldBeCachedAfterPut() throws Exception {
    String cachedValue = UUID.randomUUID().toString();
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    cache.put("foo", cachedValue);
    assertThat(cache.get("foo")).isEqualTo(cachedValue);
  }

  @Test
  public void shouldIncreaseLoadExceptionCountWhenNoLoaderIsAvailable() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithoutLoader();

    assertThrows(UnsupportedOperationException.class, () -> cache.get("foo"));

    assertThat(cache.stats().loadExceptionCount()).isEqualTo(1);
    assertThat(cache.stats().loadSuccessCount()).isEqualTo(0);
  }

  @Test
  public void shouldIncreaseLoadExceptionCountWhenLoaderThrows() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader();

    assertThrows(
        ExecutionException.class,
        () ->
            cache.get(
                "foo",
                () -> {
                  throw new Exception("Boom!");
                }));

    assertThat(cache.stats().loadExceptionCount()).isEqualTo(1);
    assertThat(cache.stats().loadSuccessCount()).isEqualTo(0);
  }

  @Test
  public void shouldIncreaseLoadSuccessCountWhenValueIsLoadedFromCallableLoader() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader(null);

    cache.get("foo", () -> "some-value");

    assertThat(cache.stats().loadSuccessCount()).isEqualTo(1);
    assertThat(cache.stats().loadExceptionCount()).isEqualTo(0);
  }

  @Test
  public void getIfPresentShouldReturnNullWhenValueIsExpired() throws Exception {
    ChronicleMapCacheImpl<String, String> cache = newCache(true, null, Duration.ofSeconds(1), null);
    cache.put("foo", "some-stale-value");
    Thread.sleep(1000); // Allow cache entry to expire
    assertThat(cache.getIfPresent("foo")).isNull();
  }

  @Test
  public void getShouldRefreshValueWhenExpired() throws Exception {
    String newCachedValue = UUID.randomUUID().toString();
    ChronicleMapCacheImpl<String, String> cache =
        newCache(true, newCachedValue, null, Duration.ofSeconds(1));
    cache.put("foo", "some-stale-value");
    Thread.sleep(1000); // Allow cache to be flagged as needing refresh
    assertThat(cache.get("foo")).isEqualTo(newCachedValue);
  }

  @Test
  public void shouldPruneExpiredValues() throws Exception {
    ChronicleMapCacheImpl<String, String> cache = newCache(true, null, Duration.ofSeconds(1), null);
    cache.put("foo1", "some-stale-value1");
    cache.put("foo2", "some-stale-value1");
    Thread.sleep(1000); // Allow cache entries to expire
    cache.put("foo3", "some-fresh-value3");
    cache.prune();

    assertThat(cache.size()).isEqualTo(1);
    assertThat(cache.get("foo3")).isEqualTo("some-fresh-value3");
  }

  @Test
  public void shouldLoadNewValueAfterBeingInvalidated() throws Exception {
    String cachedValue = UUID.randomUUID().toString();
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithLoader(cachedValue);
    cache.put("foo", "old-value");
    cache.invalidate("foo");

    assertThat(cache.size()).isEqualTo(0);
    assertThat(cache.get("foo")).isEqualTo(cachedValue);
  }

  @Test
  public void shouldClearAllEntriesWhenInvalidateAll() throws Exception {
    final ChronicleMapCacheImpl<String, String> cache = newCacheWithoutLoader();
    cache.put("foo1", "some-value");
    cache.put("foo2", "some-value");

    cache.invalidateAll();

    assertThat(cache.size()).isEqualTo(0);
  }

  private ChronicleMapCacheImpl<String, String> newCache(
      Boolean withLoader,
      @Nullable String cachedValue,
      @Nullable Duration expireAfterWrite,
      @Nullable Duration refreshAfterWrite)
      throws IOException {
    TestPersistentCacheDef cacheDef = new TestPersistentCacheDef(cachedValue);

    ChronicleMapCacheConfig config =
        new ChronicleMapCacheConfig(
            gerritConfig,
            sitePaths,
            cacheDef.name(),
            cacheDef.configKey(),
            cacheDef.diskLimit(),
            expireAfterWrite != null ? expireAfterWrite : Duration.ZERO,
            refreshAfterWrite != null ? refreshAfterWrite : Duration.ZERO);

    return new ChronicleMapCacheImpl<>(cacheDef, config, withLoader ? cacheDef.loader() : null);
  }

  private ChronicleMapCacheImpl<String, String> newCacheWithLoader(@Nullable String cachedValue)
      throws IOException {
    return newCache(true, cachedValue, null, null);
  }

  private ChronicleMapCacheImpl<String, String> newCacheWithLoader() throws IOException {
    return newCache(true, null, null, null);
  }

  private ChronicleMapCacheImpl<String, String> newCacheWithoutLoader() throws IOException {
    return newCache(false, null, null, null);
  }

  public static class TestPersistentCacheDef implements PersistentCacheDef<String, String> {

    private final String loadedValue;

    TestPersistentCacheDef(@Nullable String loadedValue) {

      this.loadedValue = loadedValue;
    }

    @Override
    public long diskLimit() {
      return 0;
    }

    @Override
    public int version() {
      return 0;
    }

    @Override
    public CacheSerializer<String> keySerializer() {
      return StringCacheSerializer.INSTANCE;
    }

    @Override
    public CacheSerializer<String> valueSerializer() {
      return StringCacheSerializer.INSTANCE;
    }

    @Override
    public String name() {
      return "chronicle-map-test-cache";
    }

    @Override
    public String configKey() {
      return name();
    }

    @Override
    public TypeLiteral<String> keyType() {
      return new TypeLiteral<String>() {};
    }

    @Override
    public TypeLiteral<String> valueType() {
      return new TypeLiteral<String>() {};
    }

    @Override
    public long maximumWeight() {
      return 0;
    }

    @Override
    public Duration expireAfterWrite() {
      return Duration.ZERO;
    }

    @Override
    public Duration expireFromMemoryAfterAccess() {
      return Duration.ZERO;
    }

    @Override
    public Duration refreshAfterWrite() {
      return Duration.ZERO;
    }

    @Override
    public Weigher<String, String> weigher() {
      return (s, s2) -> 0;
    }

    @Override
    public CacheLoader<String, String> loader() {
      return new CacheLoader<String, String>() {
        @Override
        public String load(String s) {
          return loadedValue != null ? loadedValue : UUID.randomUUID().toString();
        }
      };
    }
  }
}
