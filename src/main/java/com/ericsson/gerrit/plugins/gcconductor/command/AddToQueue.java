// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ericsson.gerrit.plugins.gcconductor.command;

import static com.google.gerrit.sshd.CommandMetaData.Mode.MASTER_OR_SLAVE;

import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.Hostname;
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.LocalDiskRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.sshd.AdminHighPriorityCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@AdminHighPriorityCommand
@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
@CommandMetaData(
    name = "add-to-queue",
    description = "Add a repository to gc queue",
    runsAt = MASTER_OR_SLAVE)
final class AddToQueue extends SshCommand {
  @Argument(index = 0, required = true, metaVar = "REPOSITORY")
  private String repository;

  @Option(name = "--first", usage = "add repository as first priority in GC queue")
  private boolean first;

  @Inject private GcQueue queue;

  @Inject @Hostname private String hostName;

  @Inject private GitRepositoryManager gitRepositoryManager;

  @Inject private ProjectCache projectCache;

  @Override
  protected void run() throws UnloggedFailure {
    try {
      Path repositoryPath = Paths.get(repository);
      if (repositoryPath.toFile().exists()) {
        repositoryPath = repositoryPath.toRealPath();
      }
      if (!FileKey.isGitRepository(repositoryPath.toFile(), FS.DETECTED)) {
        repositoryPath = resolvePath();
      }
      repository = repositoryPath.toString();
      queue.add(repository, hostName);
      if (first) {
        queue.bumpToFirst(repository);
      }
      stdout.println(String.format("%s was added to GC queue", repository));
    } catch (IOException | GcQueueException e) {
      throw die(e);
    }
  }

  private Path resolvePath() throws UnloggedFailure {
    if (!(gitRepositoryManager instanceof LocalDiskRepositoryManager)) {
      throw die("Unable to resolve path to " + repository);
    }
    String projectName = extractFrom(repository);
    Project.NameKey nameKey = new Project.NameKey(projectName);
    if (projectCache.get(nameKey) == null) {
      throw die(String.format("Repository %s not found", repository));
    }
    LocalDiskRepositoryManager localDiskRepositoryManager =
        (LocalDiskRepositoryManager) gitRepositoryManager;
    try {
      return localDiskRepositoryManager
          .getBasePath(nameKey)
          .resolve(projectName.concat(Constants.DOT_GIT_EXT))
          .toRealPath();
    } catch (IOException e) {
      throw die(e);
    }
  }

  private String extractFrom(String path) {
    String name = path;
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    if (name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    if (name.endsWith(Constants.DOT_GIT_EXT)) {
      name = name.substring(0, name.indexOf(Constants.DOT_GIT_EXT));
    }
    return name;
  }
}
