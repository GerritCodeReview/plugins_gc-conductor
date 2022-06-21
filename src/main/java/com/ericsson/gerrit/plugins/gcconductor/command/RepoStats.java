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

import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.sshd.AdminHighPriorityCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.internal.storage.file.GC.RepoStatistics;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.kohsuke.args4j.Argument;

@AdminHighPriorityCommand
@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
@CommandMetaData(
    name = "repo-stats",
    description = "display repo dirtiness statistics",
    runsAt = MASTER_OR_SLAVE)
final class RepoStats extends SshCommand {
  @Argument(index = 0, required = true, metaVar = "REPOSITORY")
  private String repository;

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
        repositoryPath = SshUtil.resolvePath(gitRepositoryManager, projectCache, repository);
      }

      stdout.println(getRepoStatistics(repositoryPath.toString()));
    } catch (IOException e) {
      throw die(e);
    }
  }

  private RepoStatistics getRepoStatistics(String repositoryPath) throws UnloggedFailure {
    try (FileRepository repository =
        (FileRepository)
            RepositoryCache.open(FileKey.exact(new File(repositoryPath), FS.DETECTED))) {
      return new GC(repository).getStatistics();
    } catch (IOException e) {
      throw die(e);
    }
  }
}
