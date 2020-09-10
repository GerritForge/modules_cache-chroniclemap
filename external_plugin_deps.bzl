load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "chronicle-map",
        artifact = "net.openhft:chronicle-map:3.19.40",
        sha1 = "820edb9aad86adb2a836b4f66a878d6101bbee54",
    )

    maven_jar(
        name = "chronicle-core",
        artifact = "net.openhft:chronicle-core:2.19.42",
        sha1 = "07d6f6902bcb13a28b18f97e92f96e033b446302",
    )

    maven_jar(
        name = "chronicle-wire",
        artifact = "net.openhft:chronicle-wire:2.19.42",
        sha1 = "dcf9382bdcda9fbfe7930d4e1e0a2ad3e22391e7",
    )

    maven_jar(
        name = "chronicle-bytes",
        artifact = "net.openhft:chronicle-bytes:2.19.42",
        sha1 = "f30b10cec1dcf42ea971f43b83a12d68a6d99fb5",
    )

    maven_jar(
        name = "chronicle-algo",
        artifact = "net.openhft:chronicle-algorithms:2.19.42",
        sha1 = "c0178f4233661a37d1d9ec917b30c99deee34afe",
    )

    maven_jar(
        name = "chronicle-values",
        artifact = "net.openhft:chronicle-values:2.19.42",
        sha1 = "f8c4a3fb9ce471a2b5d942010a914074f01f2871",
    )

    maven_jar(
        name = "chronicle-threads",
        artifact = "net.openhft:chronicle-threads:2.19.42",
        sha1 = "cd59320e912092406225187f6808f624cb4e2670",
    )

    maven_jar(
        name = "javapoet",
        artifact = "com.squareup:javapoet:1.13.0",
        sha1 = "d6562d385049f35eb50403fa86bb11cce76b866a",
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