remove-from-queue
=====================

NAME
----
remove-from-queue - Remove repository form GC queue.

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ remove-from-queue <REPOSITORY> [--force]

DESCRIPTION
-----------
Remove repository form GC queue. Can be used to clean up the queue in case of gc-executor failure.
With force option it allowing to remove repository when it is already pickup by gc-executor.
This doesn't stop executor gc-process. Force option should be use when gc-executor cannot/should not
continue with gc-execution.

ACCESS
------
Any user who has configured an SSH key and has been granted the
`Administrate Server` global capability.

SCRIPTING
---------
This command is intended to be used in a script.

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
