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
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.sshd.AdminHighPriorityCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@AdminHighPriorityCommand
@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
@CommandMetaData(
    name = "remove-from-queue",
    description = "remove repo form queue",
    runsAt = MASTER_OR_SLAVE)
final class RemoveFromQueue extends SshCommand {
  @Argument(index = 0, required = true, metaVar = "REPOSITORY")
  private String repository;

  @Option(name = "--force", usage = "remove repository if repository is picked up by GC-Executor")
  private boolean force;

  @Inject private GcQueue queue;

  @Override
  protected void run() throws UnloggedFailure {
    try {
      if (!queue.contains(repository)) {
        throw die(String.format("%s is not in the queue", repository));
      }
      if(!force && isPickUp()){
        throw die(String.format("%s repository already picked up by GC-executor", repository));
      }
      queue.remove(repository);

    } catch (GcQueueException e) {
      throw die(e);
    }
  }

  private boolean isPickUp() throws GcQueueException {
    for(RepositoryInfo repositoryInfo:queue.list()){
      if(repository.equals(repositoryInfo.getPath()) && null != repositoryInfo.getExecutor() && !repositoryInfo.getExecutor().isEmpty()){
        return true;
      }
    }
    return  false;
  }
}
