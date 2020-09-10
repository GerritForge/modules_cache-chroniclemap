load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "chronicle-map",
        artifact = "net.openhft:chronicle-map:3.20.3",
        sha1 = "9c001ad917664572f4eff34817b2344625737717",
    )
