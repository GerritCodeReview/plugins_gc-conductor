This plugin provides an automated way of detecting, managing and cleaning up
(garbage collecting) the 'dirty' repositories in a Gerrit instance.

It has two components: gc-conductor and gc-executor.

gc-conductor is a Gerrit plugin deployed in the plugins folder of a Gerrit site.
Its main function is to evaluate the dirtiness of repositories and add them to a
queue of repositories to be garbage collected. This queue is maintained as a
database in a postgresql server.

gc-executor is a runnable jar that picks up the repositories from the queue and
performs the garbage collection operation on them. gc-executor can be deployed
in the same machine that hosts the Gerrit application or in a different machine
that has access to the repositories and the postgresql server holding the queue.

Instructions to build gc-conductor and gc-executor can be found in the
[build documentation][build]. For configuring the two components, see the
[configuration file][config].

The plugin also provides SSH commands to help managing the repositories queue:

* _add-to-queue_ allows to manually add a repository to the queue.
* _bump-to-first_ increases the priority of a repository by promoting it to the
   front of the queue.
* _show-queue_ shows the list of repositories in the queue.
* _set-queued-from_ allows to change the identifier of the Gerrit instance that
  added the repository to the queue. This command is mainly useful in the context
  of an active-passive redundant configuration when the gc-executor runs in the
  same machine as the Gerrit application: by default in these cases, gc-executor
  only picks the repositories that have been added to the queue by the Gerrit
  instance running in the same host. If one of the executors goes down, it can be
  helpful to be able to change the _set_queued_from_ field, so that the running
  one can pick up repositories that were not initially added to the queue by its
  corresponding Gerrit instance.
<<<<<<< HEAD
* _repo-stats_ Display a repository dirtiness statistics
* _remove-from-queue_ Remove repository form GC queue.

[build]: build.html
[config]: config.html
