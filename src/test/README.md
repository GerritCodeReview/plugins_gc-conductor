# About this directory structure

```bash
  ./resources/com
  ./scala
```

To start using the files under these directories above, consider the
[instructions](https://gerrit-documentation.storage.googleapis.com/Documentation/3.0.9/dev-e2e-tests.html)
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
