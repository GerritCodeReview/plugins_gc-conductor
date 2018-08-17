// Copyright (C) 2016 Ericsson
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
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.sshd.AdminHighPriorityCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;
import org.kohsuke.args4j.Argument;

@AdminHighPriorityCommand
@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
@CommandMetaData(
    name = "set-queued-from",
    description = "Set queued from for all unassigned repositories",
    runsAt = MASTER_OR_SLAVE)
final class SetQueuedFrom extends SshCommand {

  @Argument(index = 0, required = true, metaVar = "HOSTAME")
  private String hostname;

  @Inject private GcQueue queue;

  @Override
  protected void run() throws UnloggedFailure {
    try {
      queue.resetQueuedFrom(hostname);
    } catch (GcQueueException e) {
      throw die(e);
    }
  }
}
