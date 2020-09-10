load("//tools/bzl:maven_jar.bzl", "maven_jar")

CHRONICLE_VER="3.19.41"

def external_plugin_deps():
    maven_jar(
        name = "chronicle-map",
        artifact = "net.openhft:chronicle-map:" + CHRONICLE_VER,
        sha1 = "9f8af40d02a33477ef4a76073a44150b622580df",
    )

    maven_jar(
        name = "chronicle-core",
        artifact = "net.openhft:chronicle-core:2.19.54",
        sha1 = "52eeeed3822cf5db3fbbf861e144c09a26d3b854",
    )

    maven_jar(
        name = "chronicle-wire",
        artifact = "net.openhft:chronicle-wire:2.19.50",
        sha1 = "f0367cbb8d27674ae02808197d32eb2693047b03",
    )

    maven_jar(
        name = "chronicle-bytes",
        artifact = "net.openhft:chronicle-bytes:2.19.49",
        sha1 = "6f95455175aca23f196f0a126ab06a49c59bea6a",
    )

    maven_jar(
        name = "chronicle-algo",
        artifact = "net.openhft:chronicle-algorithms:2.19.41",
        sha1 = "0f106926db02fc12caac0ffcd6a2740254c39f84",
    )

    maven_jar(
        name = "chronicle-values",
        artifact = "net.openhft:chronicle-values:2.19.42",
        sha1 = "f8c4a3fb9ce471a2b5d942010a914074f01f2871",
    )

    maven_jar(
        name = "chronicle-threads",
        artifact = "net.openhft:chronicle-threads:2.19.50",
        sha1 = "8512352af43cf75bbd9a76a9c4913085bb2f38f5",
    )

    maven_jar(
        name = "javapoet",
        artifact = "com.squareup:javapoet:1.12.1",
        sha1 = "e0e49f502697522ef047470b117ff81edc9f9a07",
    )

    maven_jar(
        name = "jna-platform",
        artifact = "net.java.dev.jna:jna-platform:5.5.0",
        sha1 = "af38e7c4d0fc73c23ecd785443705bfdee5b90bf",
    )

    maven_jar(
        name = "dev-jna",
        artifact = "net.java.dev.jna:jna:5.5.0",
        sha1 = "0e0845217c4907822403912ad6828d8e0b256208",
    )
