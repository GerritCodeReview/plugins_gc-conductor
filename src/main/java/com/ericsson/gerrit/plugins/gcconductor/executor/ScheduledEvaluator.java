// Copyright (C) 2017 Ericsson
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

import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Singleton
class ScheduledEvaluator implements ShutdownListener {

  private final ScheduledEvaluationTask evaluationTask;

  private ScheduledThreadPoolExecutor scheduledExecutor;

  @Inject
  public ScheduledEvaluator(ScheduledEvaluationTask evaluationTask) {
    this.evaluationTask = evaluationTask;
  }

  public void scheduleWith(long initialDelay, long interval) {
    ThreadFactory threadFactory =
        new ThreadFactoryBuilder().setNameFormat("ScheduledEvaluator-%d").build();
    scheduledExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
    scheduledExecutor.scheduleAtFixedRate(
        evaluationTask, initialDelay, interval, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onShutdown() {
    if (scheduledExecutor != null) {
      scheduledExecutor.shutdownNow();
    }
  }
}
