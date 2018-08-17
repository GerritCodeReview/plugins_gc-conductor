show-queue
=====================

NAME
----
show-queue - Show GC queue

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ show-queue

DESCRIPTION
-----------
Display repositories in gc queue and the executor handling the repository.

ACCESS
------
Any user who has configured an SSH key and has been granted the
`Administrate Server` global capability.

SCRIPTING
---------
This command is intended to be used on a need basis by the admins but could also
be used in a script.

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
