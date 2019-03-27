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

package com.ericsson.gerrit.plugins.gcconductor.executor;

import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class GcWorker extends Thread {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final GcQueue queue;
  private final GarbageCollector gc;
  private final Optional<String> queuedFrom;
  private final int queuedForLongerThan;
  private final String name;
  private final CancellableProgressMonitor cpm;
  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfRuntimeException()
          .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(3))
          .build();

  interface Factory {
    GcWorker create(String name);
  }

  @Inject
  GcWorker(
      GcQueue queue,
      GarbageCollector gc,
      CancellableProgressMonitor cpm,
      @QueuedFrom Optional<String> queuedFrom,
      @QueuedForLongerThan int queuedForLongerThan,
      @Assisted String name) {
    this.queue = queue;
    this.gc = gc;
    this.cpm = cpm;
    this.queuedFrom = queuedFrom;
    this.queuedForLongerThan = queuedForLongerThan;
    this.name = name;
    setName(name);
  }

  @Override
  public void run() {
    while (!cpm.isCancelled()) {
      String repoPath = pickRepository();
      if (repoPath != null) {
        runGc(repoPath);
      } else {
        log.atFine().log("No repository picked, going to sleep");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          log.atFine().log("Gc task was interrupted while waiting to pick a repository");
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private String pickRepository() {
    try {
      RepositoryInfo repoInfo = queue.pick(name, queuedForLongerThan, queuedFrom);
      if (repoInfo != null) {
        return repoInfo.getPath();
      }
    } catch (GcQueueException e) {
      log.atSevere().withCause(e).log("Unable to pick repository from the queue");
    }
    return null;
  }

  private void runGc(String repoPath) {
    try {
      log.atInfo().log("Starting gc on repository %s", repoPath);
      gc.setRepositoryPath(repoPath);
      gc.setPm(cpm);
      retryer.call(gc);
      log.atInfo().log("Gc completed on repository %s", repoPath);
    } catch (Throwable e) {
      if (!cpm.isCancelled()) {
        log.atSevere().withCause(e).log(
            "Gc failed on repository %s. Error Message: %s", repoPath, e.getMessage());
      }
    } finally {
      if (cpm.isCancelled()) {
        log.atWarning().log("Gc on repository %s was cancelled", repoPath);
        unpickRepository(repoPath);
      } else {
        removeRepoFromQueue(repoPath);
      }
    }
  }

  void shutdown() {
    cpm.cancel();
    this.interrupt();
  }

  private void unpickRepository(String repoPath) {
    try {
      queue.unpick(repoPath);
      log.atFine().log("Executor was removed for repository %s", repoPath);
    } catch (GcQueueException e) {
      log.atSevere().withCause(e).log("Unable to remove executor for repository %s", repoPath);
    }
  }

  private void removeRepoFromQueue(String repoPath) {
    try {
      queue.remove(repoPath);
      log.atFine().log("Repository %s was removed", repoPath);
    } catch (GcQueueException e) {
      log.atSevere().withCause(e).log("Unable to remove repository %s from the queue", repoPath);
    }
  }
}
