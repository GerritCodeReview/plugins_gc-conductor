# About this directory structure

```bash
  ./resources/com
  ./scala
```

To start using the files under these directories above, consider the
[instructions](https://gerrit-documentation.storage.googleapis.com/Documentation/3.1.5/dev-e2e-tests.html)
on how to use Gerrit core's Gatling framework. These are about running
non-core test scenarios such as this plugin one below:

```bash
  sbt "gatling:testOnly com.ericsson.gerrit.plugins.gcconductor.scenarios.CreateChangesTriggeringGc"

```

This is a scenario that can serve as an example for how to start testing
this plugin, along with its executor component. Both of these components
should be locally installed along with default configuration. Plugin's
gc-executor component is assumed to be running alongside Gerrit.

Scenario scala source files and their companion json resource ones are
stored under the usual src/test directories. That structure follows the
scala package one from the scenario classes. The core framework expects
such a directory structure for both the scala and resources (json data)
files.

There are two environment properties that can be configured:

The ```minute_multiplier``` property defines a value that get
multiplied by 60 to represent the time needed by the test before
creating the last change which triggers the plugin. Its default is ```1```
and can be set using another value:

```bash
   -Dcom.ericsson.gerrit.plugins.gcconductor.scenarios.minute_multiplier=1
```

The ```loose_objects``` property represents the value of loose objects
required to trigger garbage collection. Its default value is ```400``` and
can be set using another value:

```bash
   -Dcom.ericsson.gerrit.plugins.gcconductor.scenarios.loose_objects=400
```

This value will then be multiplied by 4 during the test. Therefore the
product must be high enough in order to trigger garbage collection.

