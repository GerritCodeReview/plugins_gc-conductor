load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

CONDUCTOR_DEPS = [
    "@postgresql//jar",
    "@dbcp//jar",
    "@pool//jar",
]

EXECUTOR_DEPS = CONDUCTOR_DEPS + [
    "@jgit//jar",
    "@javaewah//jar",
    "@guava//jar",
    "@guice//jar",
    "@guice-assistedinject//jar",
    "@javax_inject//jar",
    "@aopalliance//jar",
    "@slf4j-api//jar",
    "@slf4j-ext//jar",
    "@log4j-slf4j-impl//jar",
    "@log4j-api//jar",
    "@log4j-core//jar",
    "@retry//jar",
]

gerrit_plugin(
    name = "gc-conductor",
    srcs = glob(
        ["src/main/java/**/*.java"],
        exclude = ["**/executor/**"],
    ),
    manifest_entries = [
        "Gerrit-PluginName: gc-conductor",
        "Gerrit-Module: com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorModule",
        "Gerrit-SshModule: com.ericsson.gerrit.plugins.gcconductor.command.SshModule",
        "Implementation-Title: gc-conductor plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/admin/repos/plugins/gc-conductor",
        "Implementation-Vendor: Ericsson",
    ],
    resources = glob(
        ["src/main/resources/**/*"],
        exclude = ["src/main/resources/log4j2.xml"],
    ),
    deps = CONDUCTOR_DEPS,
)

java_library(
    name = "gc-executor_lib",
    srcs = glob([
        "src/main/java/com/ericsson/gerrit/plugins/gcconductor/*.java",
        "src/main/java/com/ericsson/gerrit/plugins/gcconductor/executor/*.java",
        "src/main/java/com/ericsson/gerrit/plugins/gcconductor/postgresqueue/*.java",
    ]),
    resources = glob([
        "bin/**/*",
        "src/main/resources/log4j2.xml",
    ]),
    deps = EXECUTOR_DEPS,
)

java_binary(
    name = "gc-executor",
    main_class = "com.ericsson.gerrit.plugins.gcconductor.executor.GcExecutor",
    runtime_deps = [":gc-executor_lib"],
)

junit_tests(
    name = "gc-conductor_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/**/*"]),
    tags = [
        "docker",
        "gc-conductor",
    ],
    deps = [
        ":gc-conductor__plugin_test_deps",
    ],
)

java_library(
    name = "gc-conductor__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = EXECUTOR_DEPS + PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":gc-conductor__plugin",
        ":gc-executor_lib",
        "@byte-buddy//jar",
        "@duct_tape//jar",
        "@jna//jar",
        "@mockito//jar",
        "@objenesis//jar",
        "@testcontainers-database-commons//jar",
        "@testcontainers-jdbc//jar",
        "@testcontainers-postgres//jar",
        "@testcontainers//jar",
        "@visible_assertions//jar",
    ],
)
