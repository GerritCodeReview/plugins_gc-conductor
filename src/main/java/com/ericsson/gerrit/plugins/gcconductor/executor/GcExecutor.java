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
import com.ericsson.gerrit.plugins.gcconductor.Hostname;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class GcExecutor implements ShutdownListener {
  private static final Logger log = LoggerFactory.getLogger(GcExecutor.class);

  static final String CONFIG_FILE_PROPERTY = "configFile";

  private final List<GcWorker> workers = new ArrayList<>();

  private final GcQueue queue;

  private final String hostname;

  @Inject
  GcExecutor(
      GcQueue queue,
      ExecutorConfig config,
      GcWorker.Factory gcWorkerFactory,
      ScheduledEvaluator scheduledEvaluator,
      @Hostname String hostname) {
    this.hostname = hostname;
    this.queue = queue;
    unpickRepositories(queue, hostname);
    startExecutors(config, gcWorkerFactory, hostname);
    scheduleEvaluation(config, scheduledEvaluator);
  }

  private void unpickRepositories(GcQueue queue, String hostname) {
    try {
      for (RepositoryInfo queuedRepo : queue.list()) {
        String executor = queuedRepo.getExecutor();
        if (executor != null && executor.startsWith(hostname)) {
          queue.unpick(queuedRepo.getPath());
        }
      }
    } catch (GcQueueException e) {
      log.error("Failed to clear assigned repositories {}", e.getMessage(), e);
    }
  }

  private void startExecutors(
      ExecutorConfig config, GcWorker.Factory gcWorkerFactory, String hostname) {
    log.info("Starting executors...");
    synchronized (this) {
      for (int i = 0; i < config.getExecutors(); i++) {
        GcWorker worker = gcWorkerFactory.create(hostname + "-" + i);
        worker.start();
        workers.add(worker);
      }
    }
  }

  private void scheduleEvaluation(ExecutorConfig config, ScheduledEvaluator scheduledEvaluator) {
    if (shouldScheduleEvaluation(config)) {
      scheduledEvaluator.scheduleWith(config.getInitialDelay(), config.getInterval());
    }
  }

  private boolean shouldScheduleEvaluation(ExecutorConfig config) {
    return config.getInitialDelay() > 0 && config.getInterval() > 0;
  }

  @Override
  public void onShutdown() {
    log.info("Shutting down executors...");
    synchronized (this) {
      for (GcWorker worker : workers) {
        worker.shutdown();
      }
      for (GcWorker worker : workers) {
        try {
          worker.join(1_000);
        } catch (InterruptedException e) {
          log.warn("Wait for executors to shutdown interrupted");
          Thread.currentThread().interrupt();
        }
      }
      unpickRepositories(queue, hostname);
    }
    log.info("Executors shut down OK.");
  }

  public static void main(String[] args) {
    try {
      final Injector injector =
          Guice.createInjector(Stage.PRODUCTION, new ExecutorModule(loadConfig()));
      // get the GcExecutor class to force start up
      injector.getInstance(GcExecutor.class);
      injector.getInstance(RuntimeShutdown.class).waitFor();
    } catch (Throwable t) {
      log.error("Uncaught error:", t);
    }
    LogManager.shutdown();
  }

  @VisibleForTesting
  static Config loadConfig() {
    String configPath = System.getProperty(CONFIG_FILE_PROPERTY);
    if (configPath != null) {
      FileBasedConfig config = new FileBasedConfig(new File(configPath), FS.DETECTED);
      try {
        config.load();
        return config;
      } catch (IOException | ConfigInvalidException e) {
        log.error(
            "Unable to load configuration from file {}. Default values will be used.",
            configPath,
            e);
      }
    }
    return new Config();
  }
}
