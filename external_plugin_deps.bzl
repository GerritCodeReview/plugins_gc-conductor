load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "mockito",
        artifact = "org.mockito:mockito-core:2.28.2",
        sha1 = "91110215a8cb9b77a46e045ee758f77d79167cc0",
        deps = [
            "@byte-buddy//jar",
            "@byte-buddy-agent//jar",
            "@objenesis//jar",
        ],
    )

    BYTE_BUDDY_VERSION = "1.9.10"

    maven_jar(
        name = "byte-buddy",
        artifact = "net.bytebuddy:byte-buddy:" + BYTE_BUDDY_VERSION,
        sha1 = "211a2b4d3df1eeef2a6cacf78d74a1f725e7a840",
    )

    maven_jar(
        name = "byte-buddy-agent",
        artifact = "net.bytebuddy:byte-buddy-agent:" + BYTE_BUDDY_VERSION,
        sha1 = "9674aba5ee793e54b864952b001166848da0f26b",
    )

    maven_jar(
        name = "duct_tape",
        artifact = "org.rnorth.duct-tape:duct-tape:1.0.8",
        sha1 = "92edc22a9ab2f3e17c9bf700aaee377d50e8b530",
    )

    maven_jar(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:2.6",
        sha1 = "639033469776fd37c08358c6b92a4761feb2af4b",
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

    LOG4J2_VERS = "2.15.0"

    maven_jar(
        name = "log4j-slf4j-impl",
        artifact = "org.apache.logging.log4j:log4j-slf4j-impl:" + LOG4J2_VERS,
        sha1 = "8bb417869ab3baa19f2fc70e6d776d041f0a8ebc",
    )

    maven_jar(
        name = "log4j-core",
        artifact = "org.apache.logging.log4j:log4j-core:" + LOG4J2_VERS,
        sha1 = "ba55c13d7ac2fd44df9cc8074455719a33f375b9",
    )

    maven_jar(
        name = "log4j-api",
        artifact = "org.apache.logging.log4j:log4j-api:" + LOG4J2_VERS,
        sha1 = "4a5aa7e55a29391c6f66e0b259d5189aa11e45d0",
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
        name = "guava",
        artifact = "com.google.guava:guava:26.0-jre",
        sha1 = "6a806eff209f36f635f943e16d97491f00f6bfab",
    )

    GUICE_VERS = "4.2.1"

    maven_jar(
        name = "guice",
        artifact = "com.google.inject:guice:" + GUICE_VERS,
        sha1 = "f77dfd89318fe3ff293bafceaa75fbf66e4e4b10",
    )

    maven_jar(
        name = "guice-assistedinject",
        artifact = "com.google.inject.extensions:guice-assistedinject:" + GUICE_VERS,
        sha1 = "d327e4aee7c96f08cd657c17da231a1f4a8999ac",
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
            "org.eclipse.jgit:org.eclipse.jgit:5.1.16.202106041830-r",
        sha1 = "1b32273b9b8326a14355374702799b6cd4a94050",
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

    DOCKER_JAVA_VERS = "3.2.5"

    maven_jar(
        name = "docker-java-api",
        artifact = "com.github.docker-java:docker-java-api:" + DOCKER_JAVA_VERS,
        sha1 = "8fe5c5e39f940ce58620e77cedc0a2a52d76f9d8",
    )

    maven_jar(
        name = "docker-java-transport",
        artifact = "com.github.docker-java:docker-java-transport:" + DOCKER_JAVA_VERS,
        sha1 = "27af0ee7ebc2f5672e23ea64769497b5d55ce3ac",
    )

    # https://github.com/docker-java/docker-java/blob/3.2.5/pom.xml#L61
    # <=> DOCKER_JAVA_VERS
    maven_jar(
        name = "jackson-annotations",
        artifact = "com.fasterxml.jackson.core:jackson-annotations:2.10.3",
        sha1 = "0f63b3b1da563767d04d2e4d3fc1ae0cdeffebe7",
    )

    TESTCONTAINERS_VERS = "1.15.0"

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TESTCONTAINERS_VERS,
        sha1 = "b627535b444d88e7b14953bb953d80d9b7b3bd76",
    )

    maven_jar(
        name = "testcontainers-database-commons",
        artifact = "org.testcontainers:database-commons:" + TESTCONTAINERS_VERS,
        sha1 = "bc52637643da33f8fadfb6220cb59a7d8da37325",
    )

    maven_jar(
        name = "testcontainers-jdbc",
        artifact = "org.testcontainers:jdbc:" + TESTCONTAINERS_VERS,
        sha1 = "3626ed0746c94b337b1872afa90331bf9e2e6e16",
    )

    maven_jar(
        name = "testcontainers-postgres",
        artifact = "org.testcontainers:postgresql:" + TESTCONTAINERS_VERS,
        sha1 = "25daf6c0dd1d52e4a1b899c5b3e07b71d382c7ba",
    )
