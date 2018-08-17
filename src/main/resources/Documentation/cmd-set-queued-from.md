set-queued-from
=====================

NAME
----
set-queued-from - Set queued from for all unassigned repositories

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ set-queued-from <HOSTNAME>

DESCRIPTION
-----------
Set queued from for all unassigned repositories.

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
