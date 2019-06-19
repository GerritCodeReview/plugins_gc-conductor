Build
=====

This plugin is built with Bazel and two build modes are supported:

* Standalone
* In Gerrit tree.

Standalone build mode is recommended, as this mode doesn't require local Gerrit
tree to exist.

Successfully running some of the tests requires Docker,
which are skipped if Docker is not available.

Bazel currently does not show
link:https://github.com/bazelbuild/bazel/issues/3476[skipped tests].

## Build standalone

To build the plugin, issue the following command:

```
  bazel build @PLUGIN@
```

The output is created in

```
  bazel-genfiles/@PLUGIN@.jar
```

To package the plugin sources run:

```
  bazel build lib@PLUGIN@__plugin-src.jar
```

The output is created in:

```
  bazel-bin/lib@PLUGIN@__plugin-src.jar
```

To execute the tests run:

```
  bazel test //...
```

This project can be imported into the Eclipse IDE. Execute:

```
  ./tools/eclipse/project.py
```

to generate the required files and then import the project.


To build the executor, issue the following command:

```
  bazel build gc-executor_deploy.jar
```

The output is created in:

```
  /bazel-bin/gc-executor_deploy.jar
```

This jar should be renamed to gc-executor.jar before deployment.

## Build in Gerrit tree

Clone or link this plugin to the plugins directory of Gerrit's
source tree. Put the external dependency Bazel build file into
the Gerrit /plugins directory, replacing the existing empty one.

```
  cd gerrit/plugins
  rm external_plugin_deps.bzl
  ln -s @PLUGIN@/external_plugin_deps.bzl .
```

From Gerrit source tree issue the command:

```
  bazel build plugins/@PLUGIN@
```

The output is created in

```
  bazel-genfiles/plugins/@PLUGIN@/@PLUGIN@.jar
```

To execute the tests run:

```
  bazel test --test_tag_filters=@PLUGIN@ //...
```

or filtering using the comma separated tags:

````
  bazel test --test_tag_filters=@PLUGIN@ --strict_java_deps=off //...
````

This project can be imported into the Eclipse IDE.
Add the plugin name to the `CUSTOM_PLUGINS` set in
Gerrit core in `tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

[Back to @PLUGIN@ documentation index][index]

[index]: index.html