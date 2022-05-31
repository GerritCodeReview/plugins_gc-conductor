# Gerrit gc-conductor docker setup

The Docker Compose project in the docker directory contains a simple
test environment consisting of PostgreSQL database and a Gerrit website
with gc-conductor and gc-executor plugins.

## How to build and run

Use docker-compose to build and run environment following the steps below:

1. Build gc-executor_deploy.jar and place it to the directory where you'd like to use it from
2. Go to docker directory: plugins/gc-conductor/src/test/docker
3. Run docker_setup.sh script with three arguments:

```
--gc-conductor-path = path_to_gc_conductor.jar
--gc-executor-path = path_to_gc_executor_deploy.jar
--postgres-driver-path = path_to_postgresql-42.2.5.jar
```

The example bellow is the command to kick off the environment setup
(expression in square brackets should be substituted with value):

```
$ sh docker_setup.sh \
--gc-conductor-path [path_to_gc-conductor.jar] \
--gc-executor-path [path_to_gc-executor_deploy.jar] \
--postgres-driver-path [path_to_postgresql-42.2.5.jar]
```

You can add one of two optional arguments or both of them:

```
--postgres-image-path = postgresql_image_location
--gerrit-image-path = gerrit_image_location
```

In case these parameters are not set, default values will be used:

```
--postgres-image-path = postgres
--gerrit-image-path = gerritcodereview/gerrit
```

Once done, gerrit site will be available following the link:

```
http://localhost:8080
```

## How to stop

Use docker-compose with 'down' target to stop containers:

```
$ docker-compose down
```

Please use 'down' target with -v flag to stop containers
and remove created volumes with initial setup:

```
$ docker-compose down -v
```

## How to run environment in detached mode

If you want to run docker environment in detached mode, please add --detached-mode as shown below:

```
$ sh docker_setup.sh \
--gc-conductor-path [path_to_gc-conductor.jar] \
--gc-executor-path [path_to_gc-executor_deploy.jar] \
--postgres-driver-path [path_to_postgresql-42.2.5.jar] \
--detached-mode -d
```

## Examples

Please see executable command example with values passed to arguments.
This example uses path to gc-executor as /local/tmp/gc-executor_deploy.jar,
that is provided in argument --gc-executor-path,
and downloads postgres driver from https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.25/postgresql-42.2.25.jar,
that is provided in argument --postgres-driver-path:

```
$ sh docker_setup.sh \
--gc-conductor-path /local/tmp/gc-conductor.jar \
--gc-executor-path /local/tmp/gc-executor_deploy.jar \
--postgres-driver-path https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.25/postgresql-42.2.25.jar
```

Please see the same command but with optional arguments, where
postgreSQL image source is provided in optional argument --postgres-image-path,
gerrit image source is provided in optional argument --gerrit-image-path:

```
sh docker_setup.sh \
--gc-conductor-path /local/tmp/gc-conductor.jar \
--gc-executor-path /local/tmp/gc-executor_deploy.jar \
--postgres-driver-path https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.25/postgresql-42.2.25.jar \
--postgres-image-path postgres \
--gerrit-image-path gerritcodereview/gerrit
```
