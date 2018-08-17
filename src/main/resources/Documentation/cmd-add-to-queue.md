add-to-queue
=====================

NAME
----
add-to-queue - Add a repository to the GC queue

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ add-to-queue <REPOSITORY> [--first]

DESCRIPTION
-----------
Add a repository to the GC queue.

An absolute path to the repository (including the .git suffix) or the project
name are accepted. A symlink pointing to a repository is also admitted.

Adding a repository to the GC queue is an idempotent operation, i.e., executing
the command multiple times only add the repository to the queue once.

ACCESS
------
Any user who has configured an SSH key and has been granted the
`Administrate Server` global capability.

SCRIPTING
---------
This command is intended to be used in a script.

OPTIONS
---------
`--first`
:	Add repository as first priority in GC queue.

EXAMPLES
--------
Absolute path to a repository:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ add-to-queue /repos/my/repo.git
```

Symlink pointing to a repository:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ add-to-queue /opt/gerrit/repos/my/repo.git
```

Name of the project:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ add-to-queue my/repo
```

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
