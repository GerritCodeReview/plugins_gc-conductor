Build
=====

This plugin is built with Bazel.
 
This plugin has a side component, gc-executor, to build as well.

Successfully running some of the tests requires Docker,
which are skipped if Docker is not available.

Bazel currently does not show
link:https://github.com/bazelbuild/bazel/issues/3476[skipped tests].

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
  bazel-bin/plugins/@PLUGIN@/@PLUGIN@.jar
```

To package the plugin sources run:

```
  bazel build plugins/@PLUGIN@:lib@PLUGIN@__plugin-src.jar
```

The output is created in:

```
  bazel-bin/plugins/@PLUGIN@/lib@PLUGIN@__plugin-src.jar
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

## Building gc-executor

To build the executor, issue the following command:

```
  bazel build plugins/@PLUGIN@:gc-executor_deploy.jar
```

The output is created in:

```
  bazel-bin/plugins/@PLUGIN@/gc-executor_deploy.jar
```

This jar should be renamed to gc-executor.jar before deployment.

Once the executor is built, the resulting postgresql jar file like below
should be manually copied over to the gerrit site /lib folder; on macOS:

```
  bazel-out/darwin-fastbuild/bin/plugins/@PLUGIN@/gc-executor.runfiles/postgresql/jar/postgresql-42.2.5.jar
```

That file has to be in accordance with potentially existing database driver
files under site's /lib, for proper account_patch_reviews support.

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
