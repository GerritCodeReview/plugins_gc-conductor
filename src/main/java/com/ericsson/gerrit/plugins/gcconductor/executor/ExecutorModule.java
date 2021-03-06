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

import com.ericsson.gerrit.plugins.gcconductor.CommonModule;
import com.ericsson.gerrit.plugins.gcconductor.Hostname;
import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.internal.UniqueAnnotations;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;

/** Configures bindings of the executor. */
class ExecutorModule extends AbstractModule {

  private final Config config;

  ExecutorModule(Config config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    install(new CommonModule(ExecutorConfig.class));
    bind(RuntimeShutdown.class);
    bind(Config.class).toInstance(config);
    bind(ExecutorConfig.class);
    install(new FactoryModuleBuilder().build(GcWorker.Factory.class));
    bind(ShutdownListener.class).annotatedWith(UniqueAnnotations.create()).to(GcExecutor.class);
    bind(CancellableProgressMonitor.class);
    bind(GarbageCollector.class);
    bind(ScheduledEvaluator.class);
    bind(ScheduledEvaluationTask.class);
  }

  @Provides
  @Singleton
  @QueuedFrom
  Optional<String> wasQueuedFrom(ExecutorConfig config, @Hostname String hostname) {
    return config.isPickOwnHostOnly() ? Optional.of(hostname) : Optional.empty();
  }

  @Provides
  @Singleton
  @QueuedForLongerThan
  int queuedForLongerThan(ExecutorConfig config) {
    return config.getDelay();
  }
}
