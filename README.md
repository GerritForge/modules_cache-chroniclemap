# Gerrit Code Review persistent cache based on ChronicleMap

Non-blocking and super-fast on-disk cache module for Gerrit Code Review
based on [ChronicleMap on-disk implementation](https://github.com/OpenHFT/Chronicle-Map).

## Setup

* Install @PLUGIN@ module

Install the chronicle-map module into the `$GERRIT_SITE/lib` directory.

Add the cache-chroniclemap module to `$GERRIT_SITE/etc/gerrit.config` as follows:

```
[gerrit]
  installModule = com.googlesource.gerrit.modules.cache.chroniclemap.ChronicleMapCacheModule
```

For further information and supported options, refer to [config](src/main/resources/Documentation/config.md)
documentation.

## TODOs

### Cache expiry feature

chronicle-map does not have the concept of entry expiration.
To completely fulfill the contract of the
[CacheDef](https://gerrit.googlesource.com/gerrit/+/refs/heads/master/java/com/google/gerrit/server/cache/CacheDef.java)
the following methods should be honoured in the chronicle-map implementation:

            expireAfterWrite();
            expireFromMemoryAfterAccess();
            refreshAfterWrite();