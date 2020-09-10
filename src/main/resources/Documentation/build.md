# Build

This plugin is built with Bazel in-tree build.

## Build in Gerrit tree

Clone or link chronicle-map module to the plugins directory of Gerrit's
source tree. Put the external dependency Bazel build file into the
Gerrit /plugins directory, replacing the existing empty one.

```
  cd gerrit/plugins
  ln -s ../../cache-chroniclemap .
  ln -fs cache-chroniclemap/external_plugin_deps.bzl .
```

From the Gerrit source tree issue the command:

```
  bazelisk build plugins/cache-chroniclemap
```

The output is created in

```
  bazel-bin/plugins/cache-chroniclemap/cache-chroniclemap.jar
```

To execute the tests run:

```
bazelisk test plugins/cache-chroniclemap/cache-chroniclemap.jar
```