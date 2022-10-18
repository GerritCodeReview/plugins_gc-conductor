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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GcWorker extends Thread {
  private static final String CONFIG_KEY_ENABLE_NATIVE_GIT_GC = "nativegc";
  private static final Logger log = LoggerFactory.getLogger(GcWorker.class);

  private final GcQueue queue;
  private final GarbageCollector gc;
  private final Optional<String> queuedFrom;
  private final int queuedForLongerThan;
  private final String name;
  private final CancellableProgressMonitor cpm;
  private final boolean forceNativeGc;
  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfRuntimeException()
          .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(3))
          .build();

  interface Factory {
    GcWorker create(String name, boolean forceNativeGc);
  }

  @Inject
  GcWorker(
      GcQueue queue,
      GarbageCollector gc,
      CancellableProgressMonitor cpm,
      @QueuedFrom Optional<String> queuedFrom,
      @QueuedForLongerThan int queuedForLongerThan,
      @Assisted String name,
      @Assisted boolean forceNativeGc) {
    this.queue = queue;
    this.gc = gc;
    this.cpm = cpm;
    this.queuedFrom = queuedFrom;
    this.queuedForLongerThan = queuedForLongerThan;
    this.name = name;
    setName(name);
    this.forceNativeGc = forceNativeGc;
  }

  @Override
  public void run() {
    while (!cpm.isCancelled()) {
      RepositoryInfo repoInfo = pickRepository();
      if (null != repoInfo && null != repoInfo.getPath()) {
        if (forceNativeGc || getNativeGitGcModeFromRepository(repoInfo.getPath())) {
          runNativeGc(repoInfo.getPath(), repoInfo.isAggressive(), repoInfo.getQueuedAt());
        } else {
          runGc(repoInfo.getPath(), repoInfo.isAggressive(), repoInfo.getQueuedAt());
        }
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

  private RepositoryInfo pickRepository() {
    try {
      RepositoryInfo repoInfo = queue.pick(name, queuedForLongerThan, queuedFrom);
      if (repoInfo != null) {
        return repoInfo;
      }
    } catch (GcQueueException e) {
      log.error("Unable to pick repository from the queue", e);
    }
    return null;
  }

  private void runGc(String repoPath, boolean aggressive, Timestamp queuedAt) {
    try {
      log.info(
          "Starting (jgit, {}) gc on repository {}",
          aggressive ? "aggressive" : "normal",
          repoPath);
      gc.setRepositoryPath(repoPath);
      gc.setPm(cpm);
      gc.setAggressive(aggressive);
      retryer.call(gc);
      log.info(
          "Gc (jgit, {}) completed on repository {} {}",
          aggressive ? "aggressive" : "normal",
          repoPath,
          getProcessingTime(queuedAt));
    } catch (Throwable e) {
      if (!cpm.isCancelled()) {
        log.error(
            "Gc jgit failed on repository {}. Error Message: {} Cause: {}: {}",
            repoPath,
            e.getMessage(),
            e.getCause(),
            e.getCause().getStackTrace(),
            e);
      }
    } finally {
      if (cpm.isCancelled()) {
        log.warn("Gc jgit on repository {} was cancelled", repoPath);
        unpickRepository(repoPath);
      } else {
        removeRepoFromQueue(repoPath);
      }
    }
  }

  private String getProcessingTime(Timestamp queuedAt) {
    long processingTime = Timestamp.from(Instant.now()).getTime() - queuedAt.getTime();
    Duration duration = Duration.ofMillis(processingTime);
    long HH = duration.getSeconds() / 3600;
    long MM = (duration.getSeconds() % 3600) / 60;
    long SS = duration.getSeconds() % 60;
    return String.format("in %d ms [%02d:%02d:%02d]", processingTime, HH, MM, SS);
  }

  private class StreamLogger extends Thread {

    InputStream is;
    String name;
    boolean isRunning;

    StreamLogger(InputStream is, String name) {
      this.is = is;
      this.name = name;
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        isRunning = true;
        while (isRunning) {
          while ((line = br.readLine()) != null) {
            log.debug("{}:{}", name, line);
          }
          Thread.sleep(100);
        }
      } catch (Exception ioe) {
        log.debug("{} closeRead Stream reason: {}", name, ioe);
        isRunning = false;
      }
    }
  }

  private void runNativeGc(String repoPath, boolean isAggressive, Timestamp queuedAt) {
    Process _p = null;
    InputStream _in = null;
    InputStream _err = null;
    log.info(
        "Starting (native, {}) gc on repository {}",
        isAggressive ? "aggressive" : "normal",
        repoPath);
    String aggressive = isAggressive ? " --aggressive" : "";
    String command = "git gc" + aggressive;
    log.debug("command ={}", command);

    try {
      _p = Runtime.getRuntime().exec(command, null, new File(repoPath));
      _in = _p.getInputStream();
      _err = _p.getErrorStream();
      new StreamLogger(_in, "SO").start();
      new StreamLogger(_err, "EO").start();
      _p.waitFor();
      removeRepoFromQueue(repoPath);
      log.info(
          "Gc (native, {}) completed on repository {} {}",
          isAggressive ? "aggressive" : "normal",
          repoPath,
          getProcessingTime(queuedAt));
    } catch (IOException | InterruptedException e) {
      unpickRepository(repoPath);
      log.error(String.format("Gc native failed on repository %s", repoPath), e);
    } finally {
      if (_p != null) {
        close(_p.getErrorStream());
        close(_p.getInputStream());
        _p.destroy();
      }
      close(_in);
      close(_err);
    }
  }

  private static void close(InputStream anInput) {
    try {
      if (anInput != null) {
        anInput.close();
      }
    } catch (IOException e) {
      log.error("{}", e);
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

  public static boolean getNativeGitGcModeFromRepository(String repositoryPath) {
    try {
      Git git = Git.open(new File(repositoryPath));
      return git.getRepository()
          .getConfig()
          .getBoolean(ConfigConstants.CONFIG_GC_SECTION, CONFIG_KEY_ENABLE_NATIVE_GIT_GC, false);
    } catch (IOException e) {
      log.error("Error reading repository config returns default jgit-gc{}", repositoryPath, e);
    }
    return false;
  }
}
