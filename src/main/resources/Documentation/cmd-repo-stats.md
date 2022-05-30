repo-stats
=====================

NAME
----
repo-stats - Display a repository dirtiness statistics

SYNOPSIS
--------
>     ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ repo-stats <REPOSITORY>

DESCRIPTION
-----------
Display a repository dirtiness statistics.

An absolute path to the repository (including the .git suffix) or the project
name are accepted. A symlink pointing to a repository is also admitted.

Displaying statistic can be usefully to determine repo condition. Can be a part of script with 
adding a repository to the GC queue.

ACCESS
------
Any user who has configured an SSH key and has been granted the
`Administrate Server` global capability.

SCRIPTING
---------
This command is intended to be used in a script.

EXAMPLES
--------
Absolute path to a repository:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ repo-stats /repos/my/repo.git
```

Symlink pointing to a repository:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ repo-stats /opt/gerrit/repos/my/repo.git
```

Name of the project:

```
$ ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ repo-stats my/repo
```

GERRIT
------
Part of [Gerrit Code Review](../../../Documentation/index.html)
