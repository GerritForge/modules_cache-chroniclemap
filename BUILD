load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "cache-chroniclemap",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@chronicle-map//jar",
        "@chronicle-core//jar",
        "@chronicle-wire//jar",
        "@chronicle-bytes//jar",
        "@chronicle-algo//jar",
        "@chronicle-values//jar",
        "@chronicle-threads//jar",
        "@javapoet//jar",
        "@jna-platform//jar",
        "@platform//jar",
        "@dev-jna//jar",
    ],
)

junit_tests(
    name = "cache-chroniclemap_tests",
    srcs = glob(
        ["src/test/java/**/*Test.java"],
    ),
    visibility = ["//visibility:public"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":cache-chroniclemap__plugin",
        "@chronicle-bytes//jar",
    ],
)
