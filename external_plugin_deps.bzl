load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "duct_tape",
        artifact = "org.rnorth.duct-tape:duct-tape:1.0.8",
        sha1 = "92edc22a9ab2f3e17c9bf700aaee377d50e8b530",
    )

    SLF4J_VERS = "1.7.30"

    maven_jar(
        name = "slf4j-api",
        artifact = "org.slf4j:slf4j-api:" + SLF4J_VERS,
        sha1 = "b5a4b6d16ab13e34a88fae84c35cd5d68cac922c",
    )

    maven_jar(
        name = "slf4j-ext",
        artifact = "org.slf4j:slf4j-ext:" + SLF4J_VERS,
        sha1 = "595d5dabfeb29244b8c91776898cee78299080d5",
    )

    LOG4J2_VERS = "2.17.0"

    maven_jar(
        name = "log4j-slf4j-impl",
        artifact = "org.apache.logging.log4j:log4j-slf4j-impl:" + LOG4J2_VERS,
        sha1 = "1ec25ce0254749c94549ea9c3cea34bd0488c9c6",
    )

    maven_jar(
        name = "log4j-core",
        artifact = "org.apache.logging.log4j:log4j-core:" + LOG4J2_VERS,
        sha1 = "fe6e7a32c1228884b9691a744f953a55d0dd8ead",
    )

    maven_jar(
        name = "log4j-api",
        artifact = "org.apache.logging.log4j:log4j-api:" + LOG4J2_VERS,
        sha1 = "bbd791e9c8c9421e45337c4fe0a10851c086e36c",
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

    GUICE_VERS = "5.0.1"

    maven_jar(
        name = "guice",
        artifact = "com.google.inject:guice:" + GUICE_VERS,
        sha1 = "0dae7556b441cada2b4f0a2314eb68e1ff423429",
    )

    maven_jar(
        name = "guice-assistedinject",
        artifact = "com.google.inject.extensions:guice-assistedinject:" + GUICE_VERS,
        sha1 = "62e02f2aceb7d90ba354584dacc018c1e94ff01c",
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

    DOCKER_JAVA_VERS = "3.2.8"

    maven_jar(
        name = "docker-java-api",
        artifact = "com.github.docker-java:docker-java-api:" + DOCKER_JAVA_VERS,
        sha1 = "4ac22a72d546a9f3523cd4b5fabffa77c4a6ec7c",
    )

    maven_jar(
        name = "docker-java-transport",
        artifact = "com.github.docker-java:docker-java-transport:" + DOCKER_JAVA_VERS,
        sha1 = "c3b5598c67d0a5e2e780bf48f520da26b9915eab",
    )

    # https://github.com/docker-java/docker-java/blob/3.2.8/pom.xml#L61
    # <=> DOCKER_JAVA_VERS
    maven_jar(
        name = "jackson-annotations",
        artifact = "com.fasterxml.jackson.core:jackson-annotations:2.10.3",
        sha1 = "0f63b3b1da563767d04d2e4d3fc1ae0cdeffebe7",
    )

    TESTCONTAINERS_VERS = "1.15.3"

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TESTCONTAINERS_VERS,
        sha1 = "95c6cfde71c2209f0c29cb14e432471e0b111880",
    )

    maven_jar(
        name = "testcontainers-database-commons",
        artifact = "org.testcontainers:database-commons:" + TESTCONTAINERS_VERS,
        sha1 = "e63193bdf7e1cba4c743b858068289f2836eca16",
    )

    maven_jar(
        name = "testcontainers-jdbc",
        artifact = "org.testcontainers:jdbc:" + TESTCONTAINERS_VERS,
        sha1 = "8b43efd5646199955d4ad6adedc038f750feb145",
    )

    maven_jar(
        name = "testcontainers-postgres",
        artifact = "org.testcontainers:postgresql:" + TESTCONTAINERS_VERS,
        sha1 = "47d181885af4e3d3e2f775bd904e73f2210c9be0",
    )
