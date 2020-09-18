# Persistent cache for Gerrit, based on ChronicleMap

Non-blocking and super-fast on-disk cache libModule for [Gerrit Code Review](https://gerritcodereview.com)
based on [ChronicleMap on-disk implementation](https://github.com/OpenHFT/Chronicle-Map).

## How to build

This libModule is built like a Gerrit in-tree plugin, using  Bazel.

Create a symbolic link of the repsotiory source to the Gerrit source tree /plugins/cache-chronicalmap directory,
and of the external_plugin_deps.bzl dependencies to the /plugins/external_plugin_deps.bzl.

Example:

```sh
$ git clone https://gerrit.googlesource.com/gerrit
$ git clone https://github.com/GerritForge/modules_cache-chroniclemap.git
$ cd gerrit/plugins
$ ln -s ../../modules_cache-chroniclemap cache-chroniclemap
$ ln -sf ../../external_plugin_deps.bzl .
```


To build the cache-chroniclemap libModule, run `bazelsk build plugins/cache-chroniclemap` from the Gerrit source tree.
The libModule is generated under the `basel-bin/plugins/cache-chroniclemap/cache-chroniclemap.jar`.

Example:

```sh
$ cd gerrit
$ bazelisk build plugin/cache-chroniclemap
```

To run the cache-chroniclemap tests, run `bazelisk test plugins/cache-chroniclemap/...` from the Gerrit source tree.

Example:

```sh
$ cd gerrit
$ bazelisk test plugin/cache-chroniclemap/...
```

## Setup

* Install cache-chronicalmap module

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