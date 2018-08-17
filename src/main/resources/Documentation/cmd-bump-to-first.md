bump-to-first
=====================

NAME
----
bump-to-first - Update a repository to be first priority in GC queue.

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ bump-to-first <REPOSITORY>

DESCRIPTION
-----------
Update a repository to be first priority in GC queue.

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
