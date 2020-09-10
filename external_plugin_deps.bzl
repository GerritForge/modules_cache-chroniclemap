load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "chronicle-map",
        artifact = "net.openhft:chronicle-map:3.20.3",
        sha1 = "9c001ad917664572f4eff34817b2344625737717",
    )

    maven_jar(
        name = "chronicle-core",
        artifact = "net.openhft:chronicle-core:2.20.26",
        sha1 = "4beddaeaf5be425f848c63aa1474a713dcae394a",
    )

    maven_jar(
        name = "chronicle-wire",
        artifact = "net.openhft:chronicle-wire:2.20.27",
        sha1 = "0f822cfdc61c416defdd15d9968a8adb1c26942e",
    )

    maven_jar(
        name = "chronicle-bytes",
        artifact = "net.openhft:chronicle-bytes:2.20.21",
        sha1 = "83ec1cf2584e1a531b3386ef26e36b656e63ab08",
    )

    maven_jar(
        name = "chronicle-algo",
        artifact = "net.openhft:chronicle-algorithms:2.20.2",
        sha1 = "229e8ee52acb447980488c76e839243511365264",
    )

    maven_jar(
        name = "chronicle-values",
        artifact = "net.openhft:chronicle-values:2.20.6",
        sha1 = "b188c7900263ed2a5134b5c4ac5cb115e8da762a",
    )

    maven_jar(
        name = "javapoet",
        artifact = "com.squareup:javapoet:1.13.0",
        sha1 = "d6562d385049f35eb50403fa86bb11cce76b866a",
    )

    maven_jar(
        name = "chronicle-threads",
        artifact = "net.openhft:chronicle-threads:2.20.7",
        sha1 = "4cd38765d4d1bd5874602dbd0220a440067ee907",
    )

    maven_jar(
        name = "jna-platform",
        artifact = "net.java.dev.jna:jna-platform:5.6.0",
        sha1 = "d18424ffb8bbfd036d71bcaab9b546858f2ef986",
    )

    maven_jar(
        name = "platform",
        artifact = "net.java.dev.jna:platform:3.5.2",
        sha1 = "beac07d13858ef3697ceeab43897d70aeb5113c9",
    )

    maven_jar(
            name = "dev-jna",
            artifact = "net.java.dev.jna:jna:5.6.0",
            sha1 = "330f2244e9030119ab3030fc3fededc86713d9cc",
        )