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

package com.ericsson.gerrit.plugins.gcconductor.evaluator;

import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.git.WorkQueue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
class EvaluatorExecutorProvider implements Provider<ExecutorService>, ShutdownListener {
  private ExecutorService executor;

  @Inject
  EvaluatorExecutorProvider(
      WorkQueue workQueue, @PluginName String pluginName, EvaluatorConfig config) {
    executor = workQueue.createQueue(config.getThreadPoolSize(), "[" + pluginName + " plugin]");
  }

  @Override
  public void onShutdown() {
    executor.shutdownNow();
    executor = null;
  }

  @Override
  public ExecutorService get() {
    return executor;
  }
}
