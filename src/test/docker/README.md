# Gerrit gc-conductor docker setup

The Docker Compose project in the docker directory contains a simple
test environment consisting of PostgreSQL database, Gerrit website
with gc-conductor and gc-executor plugins and Apache http server. The last
is used to provide Gerrit http authorisation.

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

You can add one of three optional arguments or both of them:

```
--postgres-image-path = postgresql_image_location
--gerrit-image-path = gerrit_image_location
--httpd-image-path = http_server_image_location
```

In case these parameters are not set, default values will be used:

```
--postgres-image-path = postgres
--gerrit-image-path = gerritcodereview/gerrit
--http-server-image-path = httpd
```

Once done, gerrit site will be available following the link:

```
http://localhost:8080
```

When running Gerrit UI, use credentials:

```
username: gerrit
password: secret
```

## How to debug Gerrit server

The debug port 5005 is used when Gerrit is started. It allows user to debug Gerrit server.
To learn how to attach IntelliJ to the Gerrit server remotely, see
[Debugging a remote Gerrit server](https://gerrit-review.googlesource.com/Documentation/dev-intellij.html#remote-debug).

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
and downloads postgres driver from https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar,
that is provided in argument --postgres-driver-path:

```
$ sh docker_setup.sh \
--gc-conductor-path /local/tmp/gc-conductor.jar \
--gc-executor-path /local/tmp/gc-executor_deploy.jar \
--postgres-driver-path https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar
```

Please see the same command but with optional arguments, where
postgreSQL image source is provided in optional argument --postgres-image-path,
gerrit image source is provided in optional argument --gerrit-image-path:

```
sh docker_setup.sh \
--gc-conductor-path /local/tmp/gc-conductor.jar \
--gc-executor-path /local/tmp/gc-executor_deploy.jar \
--postgres-driver-path https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar \
--postgres-image-path postgres \
--gerrit-image-path gerritcodereview/gerrit
--httpd-image-path httpd
```

## How to build and run Gerrit using 'DEVELOPMENT_BECOME_ANY_ACCOUNT' authentication type

1. Comment apache section in docker-compose.yaml config.
2. Add port binding into 'gerrit-gc' into ports section: "8080:8080".
3. Update auth.type setting in etc/gerrit.config from 'http' to 'DEVELOPMENT_BECOME_ANY_ACCOUNT'.
4. Run build and run as usually.
