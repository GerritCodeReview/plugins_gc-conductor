load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "duct_tape",
        artifact = "org.rnorth.duct-tape:duct-tape:1.0.8",
        sha1 = "92edc22a9ab2f3e17c9bf700aaee377d50e8b530",
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
        artifact = "org.postgresql:postgresql:42.2.5",
        sha1 = "951b7eda125f3137538a94e2cbdcf744088ad4c2",
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
        name = "guava-failureaccess",
        artifact = "com.google.guava:failureaccess:1.0.1",
        sha1 = "1dcf1de382a0bf95a3d8b0849546c88bac1292c9",
    )

    maven_jar(
        name = "guava",
        artifact = "com.google.guava:guava:27.0.1-jre",
        sha1 = "bd41a290787b5301e63929676d792c507bbc00ae",
    )

    GUICE_VERS = "4.2.2"

    maven_jar(
        name = "guice",
        artifact = "com.google.inject:guice:" + GUICE_VERS,
        sha1 = "6dacbe18e5eaa7f6c9c36db33b42e7985e94ce77",
    )

    maven_jar(
        name = "guice-assistedinject",
        artifact = "com.google.inject.extensions:guice-assistedinject:" + GUICE_VERS,
        sha1 = "c33fb10080d58446f752b4fcfff8a5fabb80a449",
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
            "org.eclipse.jgit:org.eclipse.jgit:5.3.6.201910020505-r",
        sha1 = "0b9085bd16c630412bfa50bcc509dcedfbbe16d3",
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
        artifact = "net.java.dev.jna:jna:5.5.0",
        sha1 = "0e0845217c4907822403912ad6828d8e0b256208",
    )

    TESTCONTAINERS_VERS = "1.14.2"

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TESTCONTAINERS_VERS,
        sha1 = "d74bc045fb5f30988b0adff20244412972a9f564",
    )

    maven_jar(
        name = "testcontainers-database-commons",
        artifact = "org.testcontainers:database-commons:" + TESTCONTAINERS_VERS,
        sha1 = "244303d27c2816f651c6e6f7a844fa48dafcc74e",
    )

    maven_jar(
        name = "testcontainers-jdbc",
        artifact = "org.testcontainers:jdbc:" + TESTCONTAINERS_VERS,
        sha1 = "3005971b09f502b43aec3f3aea14d6cc4f7c00e5",
    )

    maven_jar(
        name = "testcontainers-postgres",
        artifact = "org.testcontainers:postgresql:" + TESTCONTAINERS_VERS,
        sha1 = "e1fa458977925a36b161ccc7f184c47a2c8544d6",
    )
