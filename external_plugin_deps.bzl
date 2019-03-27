load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "mockito",
        artifact = "org.mockito:mockito-core:2.25.1",
        sha1 = "e8fa2864b65c0b6fbb20daa436a94853bcd17e5e",
        deps = [
            "@byte-buddy//jar",
            "@byte-buddy-agent//jar",
            "@objenesis//jar",
        ],
    )

    BYTE_BUDDY_VERSION = "1.9.7"

    maven_jar(
        name = "byte-buddy",
        artifact = "net.bytebuddy:byte-buddy:" + BYTE_BUDDY_VERSION,
        sha1 = "8fea78fea6449e1738b675cb155ce8422661e237",
    )

    maven_jar(
        name = "byte-buddy-agent",
        artifact = "net.bytebuddy:byte-buddy-agent:" + BYTE_BUDDY_VERSION,
        sha1 = "8e7d1b599f4943851ffea125fd9780e572727fc0",
    )

    maven_jar(
        name = "duct_tape",
        artifact = "org.rnorth.duct-tape:duct-tape:1.0.7",
        sha1 = "a26b5d90d88c91321dc7a3734ea72d2fc019ebb6",
    )

    maven_jar(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:2.6",
        sha1 = "639033469776fd37c08358c6b92a4761feb2af4b",
    )

    SLF4J_VERS = "1.7.26"

    maven_jar(
        name = "slf4j-api",
        artifact = "org.slf4j:slf4j-api:" + SLF4J_VERS,
        sha1 = "77100a62c2e6f04b53977b9f541044d7d722693d",
    )

    maven_jar(
        name = "slf4j-ext",
        artifact = "org.slf4j:slf4j-ext:" + SLF4J_VERS,
        sha1 = "31cdf122e000322e9efcb38913e9ab07825b17ef",
    )

    LOG4J2_VERS = "2.11.1"

    maven_jar(
        name = "log4j-slf4j-impl",
        artifact = "org.apache.logging.log4j:log4j-slf4j-impl:" + LOG4J2_VERS,
        sha1 = "4b41b53a3a2d299ce381a69d165381ca19f62912",
    )

    maven_jar(
        name = "log4j-core",
        artifact = "org.apache.logging.log4j:log4j-core:" + LOG4J2_VERS,
        sha1 = "592a48674c926b01a9a747c7831bcd82a9e6d6e4",
    )

    maven_jar(
        name = "log4j-api",
        artifact = "org.apache.logging.log4j:log4j-api:" + LOG4J2_VERS,
        sha1 = "268f0fe4df3eefe052b57c87ec48517d64fb2a10",
    )

    maven_jar(
        name = "postgresql",
        artifact = "org.postgresql:postgresql:42.2.4",
        sha1 = "dff98730c28a4b3a3263f0cf4abb9a3392f815a7",
    )

    maven_jar(
        name = "dbcp",
        artifact = "commons-dbcp:commons-dbcp:1.4",
        sha1 = "30be73c965cc990b153a100aaaaafcf239f82d39",
    )

    maven_jar(
        name = "pool",
        artifact = "commons-pool:commons-pool:1.5.5",
        sha1 = "7d8ffbdc47aa0c5a8afe5dc2aaf512f369f1d19b",
    )

    maven_jar(
        name = "guava",
        artifact = "com.google.guava:guava:25.1-jre",
        sha1 = "6c57e4b22b44e89e548b5c9f70f0c45fe10fb0b4",
    )

    GUICE_VERS = "4.2.0"

    maven_jar(
        name = "guice",
        artifact = "com.google.inject:guice:" + GUICE_VERS,
        sha1 = "25e1f4c1d528a1cffabcca0d432f634f3132f6c8",
    )

    maven_jar(
        name = "guice-assistedinject",
        artifact = "com.google.inject.extensions:guice-assistedinject:" + GUICE_VERS,
        sha1 = "e7270305960ad7db56f7e30cb9df6be9ff1cfb45",
    )

    maven_jar(
        name = "aopalliance",
        artifact = "aopalliance:aopalliance:1.0",
        sha1 = "0235ba8b489512805ac13a8f9ea77a1ca5ebe3e8",
    )

    maven_jar(
        name = "javax_inject",
        artifact = "javax.inject:javax.inject:1",
        sha1 = "6975da39a7040257bd51d21a231b76c915872d38",
    )

    maven_jar(
        name = "jgit",
        artifact =
            "org.eclipse.jgit:org.eclipse.jgit:4.9.8.201812241815-r",
        sha1 = "dedb5d05a952551dc465611ebde3819d86bb22fc",
    )

    maven_jar(
        name = "javaewah",
        artifact = "com.googlecode.javaewah:JavaEWAH:1.1.6",
        sha1 = "94ad16d728b374d65bd897625f3fbb3da223a2b6",
    )

    maven_jar(
        name = "retry",
        artifact = "tech.huffman.re-retrying:re-retrying:3.0.0",
        sha1 = "bd3ce1aaafc0f357354e76890f0a8199a0f42f3a",
    )

    maven_jar(
        name = "visible_assertions",
        artifact = "org.rnorth.visible-assertions:visible-assertions:2.1.2",
        sha1 = "20d31a578030ec8e941888537267d3123c2ad1c1",
    )

    maven_jar(
        name = "jna",
        artifact = "net.java.dev.jna:jna:5.2.0",
        sha1 = "ed8b772eb077a9cb50e44e90899c66a9a6c00e67",
    )

    TEST_CONTAINERS_VERS = "1.11.1"

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TEST_CONTAINERS_VERS,
        sha1 = "502692678fc90996a6c3c5392c229725cd5d4635",
    )

    maven_jar(
        name = "testcontainers-database-commons",
        artifact = "org.testcontainers:database-commons:" + TEST_CONTAINERS_VERS,
        sha1 = "321b1d943cc3b2d3448d47a8d51478d73a5a207c",
    )

    maven_jar(
        name = "testcontainers-jdbc",
        artifact = "org.testcontainers:jdbc:" + TEST_CONTAINERS_VERS,
        sha1 = "5432618ccd7033cc992d993028e52cb9391f0cde",
    )

    maven_jar(
        name = "testcontainers-postgres",
        artifact = "org.testcontainers:postgresql:" + TEST_CONTAINERS_VERS,
        sha1 = "cd2dc98053b50e743fb0e3a1aac2b069d77da066",
    )
