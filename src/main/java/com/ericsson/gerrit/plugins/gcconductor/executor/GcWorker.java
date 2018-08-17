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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GcWorker extends Thread {
  private static final Logger log = LoggerFactory.getLogger(GcWorker.class);

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
        log.debug("No repository picked, going to sleep");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          log.debug("Gc task was interrupted while waiting to pick a repository");
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
      log.error("Unable to pick repository from the queue", e);
    }
    return null;
  }

  private void runGc(String repoPath) {
    try {
      log.info("Starting gc on repository {}", repoPath);
      gc.setRepositoryPath(repoPath);
      gc.setPm(cpm);
      retryer.call(gc);
      log.info("Gc completed on repository {}", repoPath);
    } catch (Throwable e) {
      if (!cpm.isCancelled()) {
        log.error(
            "Gc failed on repository {}. Error Message: {} Cause: {}: {}",
            repoPath,
            e.getMessage(),
            e.getCause(),
            e.getCause().getStackTrace(),
            e);
      }
    } finally {
      if (cpm.isCancelled()) {
        log.warn("Gc on repository {} was cancelled", repoPath);
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
      log.debug("Executor was removed for repository {}", repoPath);
    } catch (GcQueueException e) {
      log.error("Unable to remove executor for repository {}", repoPath, e);
    }
  }

  private void removeRepoFromQueue(String repoPath) {
    try {
      queue.remove(repoPath);
      log.debug("Repository {} was removed", repoPath);
    } catch (GcQueueException e) {
      log.error("Unable to remove repository {} from the queue", repoPath, e);
    }
  }
}
