@PLUGIN@ Configuration
======================

gc-conductor uses a postgresql database to manage the queue of repositories that
need to be garbage collected. The following commands can be used on the postgresql
database host to create the configured username/password; superuser is required:

```
  sudo /etc/init.d/postgresql start
  sudo su - postgres
  createuser -P -s -e username
```

File `gerrit.config`
--------------------

`plugin.@PLUGIN@.packed`
:  Packed threshold. By default, `40`.

`plugin.@PLUGIN@.loose`
:  Loose threshold. By default, `400`.

`plugin.@PLUGIN@.databaseUrl`
:  Database url. By default, `jdbc:postgresql://localhost:5432/`.

`plugin.@PLUGIN@.databaseName`
:  Database name. By default, `gc`.

`plugin.@PLUGIN@.databaseUrlOptions`
:  jdbc option properties to append to the database URL, example
`?ssl=true&loglevel=org.postgresql.Driver.DEBUG`. Empty by default.

`plugin.@PLUGIN@.username`
:  Database username. By default, `gc`.

`plugin.@PLUGIN@.password`
:  Database password. By default, `gc`.

`plugin.@PLUGIN@.threadPoolSize`
:  Thread pool size. By default, `4`.

`plugin.@PLUGIN@.expireTimeRecheck`
:  Time before a check is considered expired. By default, `60s`.

GC executor
--------------------

GC executor is packaged as a runnable java jar. The [build documentation][build]
details the steps to build gc-executor.jar. Once built, gc-executor.jar is deployed
to the node charged of doing the garbage collection (GC) process.

The configuration can be passed to the gc executor jar by using:

```
  java -DconfigFile=/path/to/gc.config -jar gc-executor.jar
```

### File `gc.config`

The file `gc.config` is a Git-style config file that controls several settings for
gc executor. The contents of the `gc.config` file are cached at startup. If this
file is modified, gc executor needs to be restarted in order to be able to use the
new values.

#### Sample `gc.config`:

```
[jvm]
  javaHome = /opt/gerrit/jdk8
  javaOptions = -Xrunjdwp:transport=dt_socket,address=localhost:8788,server=y,suspend=n
  javaOptions = -Xms1g
  javaOptions = -Xmx32g
  javaOptions = -XX:+UseG1GC
  javaOptions = -XX:MaxGCPauseMillis=2000

[core]
  executors = 2
  pickOwnHostOnly = false
  delay = 0

[db]
  databaseUrl = jdbc:postgresql://same_host_as_plugin:5432/
  databaseName = testDb
  databaseUrlOptions = ?ssl=true
  username = testUser
  password = testPass

[evaluation]
  packed = 40
  loose = 400
  repositoriesPath = /path/to/repositories
  startTime = Sat 22:00
  interval = 1 week
```

#### Section `jvm`

`jvm.javaHome`
:       Location of java. By default /opt/gerrit/jdk8

`jvm.javaOptions`
:       Options to pass along to the Java runtime. If multiple values are
configured, they are passed in order on the command line, separated by spaces.

#### Section `core`

`core.executors`
:       Number of executors. By default, 2.

`core.pickOwnHostOnly`
:       Whether to pick repositories added to queue from same host only or not.
By default, true.

`core.delay`
:       minimal delay in seconds a repository must be in queue before it can be
picked. By default, 0.

#### Section `db`

`db.databaseUrl`
:  Database URL. By default, `jdbc:postgresql://localhost:5432/`.

`db.databaseName`
:       Database name. By default, `gc`.

`db.databaseUrlOptions`
:  jdbc option properties to append to the database URL. For example,
`?ssl=true&loglevel=org.postgresql.Driver.DEBUG`. Empty by default.

`db.username`
:       Username to connect to the database. By default, `gc`.

`db.password`
:       Password associated to that username. By default, `gc`.

It is important to note here that the parameters used in this section should be
the same used in `gerrit.config` to define the database settings.

#### Section `evaluation`

This section allows to configure dirtiness evaluation for a list of repositories.

`evaluation.packed`
:       number of pack files in a repository for it to be considered dirty. By
default, 40.

`evaluation.loose`
:       number of loose objects in a repository for it to be considered dirty.
By default, 400.

`evaluation.repositoriesPath`
:       path to the repositories to be evaluated for dirtiness. By default,
/opt/gerrit/repos.

`evaluation.startTime`
:       start time to define the first execution of the repositories dirtiness
evaluation. Expressed as &lt;day of week> &lt;hours>:&lt;minutes>. By default,
disabled.

This setting should be expressed using the following time units:

  * &lt;day of week> : Mon, Tue, Wed, Thu, Fri, Sat, Sun
  * &lt;hours> : 00-23
  * &lt;minutes> : 00-59

`evaluation.interval`
:       interval for periodic repetition of dirtiness evaluation. By default,
disabled.

The following suffixes are supported to define the time unit for the interval:

 * h, hour, hours
 * d, day, days
 * w, week, weeks (1 week is treated as 7 days)
 * mon, month, months (1 month is treated as 30 days)

If no time unit is specified, days are assumed.

### GC execution

Executors can be started/stopped using the gc_ctl script.

```
  /opt/gerrit/gc-conductor/bin/gc_ctl {start|stop|restart|status|check}
```

[Back to @PLUGIN@ documentation index][index]

[build]: build.html
[index]: index.html
