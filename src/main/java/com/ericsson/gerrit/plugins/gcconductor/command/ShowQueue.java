// Copyright (C) 2016 The Android Open Source Project
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
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.google.common.base.Strings;
import com.google.gerrit.common.TimeUtil;
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.sshd.AdminHighPriorityCommand;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@AdminHighPriorityCommand
@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
@CommandMetaData(name = "show-queue", description = "Show GC queue", runsAt = MASTER_OR_SLAVE)
final class ShowQueue extends SshCommand {

  @Inject private GcQueue queue;

  @Override
  protected void run() throws UnloggedFailure {
    try {
      List<RepositoryInfo> repositories = queue.list();

      // Find width of executor and queuedFrom column. Typical executor names
      // consist of 11 characters hostname suffixed by "-n" and typical queued
      // from hostname consist of 11 characters
      int executorColumnWidth = 13;
      int queuedFromColumnWidth = 11;
      for (RepositoryInfo repositoryInfo : repositories) {
        if (repositoryInfo.getExecutor() != null) {
          executorColumnWidth =
              Math.max(executorColumnWidth, repositoryInfo.getExecutor().length());
        }
        queuedFromColumnWidth =
            Math.max(queuedFromColumnWidth, repositoryInfo.getQueuedFrom().length());
      }

      String format = "%-12s %-" + executorColumnWidth + "s %-" + queuedFromColumnWidth + "s %s\n";
      stdout.print(String.format(format, "Queued At", "Executor", "Queued From", "Repository"));
      stdout.print(
          "------------------------------------------------------------------------------\n");
      for (RepositoryInfo repositoryInfo : repositories) {
        stdout.print(
            String.format(
                format,
                queuedAt(repositoryInfo.getQueuedAt()),
                Strings.nullToEmpty(repositoryInfo.getExecutor()),
                repositoryInfo.getQueuedFrom(),
                repositoryInfo.getPath()));
      }
      stdout.print(
          "------------------------------------------------------------------------------\n");
      stdout.print(
          "  "
              + repositories.size()
              + " repositor"
              + (repositories.size() > 1 ? "ies" : "y")
              + "\n");
    } catch (Exception e) {
      throw die(e);
    }
  }

  private static String queuedAt(Date when) {
    if (TimeUtil.nowMs() - when.getTime() < 24 * 60 * 60 * 1000L) {
      return new SimpleDateFormat("HH:mm:ss.SSS").format(when);
    }
    return new SimpleDateFormat("MMM-dd HH:mm").format(when);
  }
}
