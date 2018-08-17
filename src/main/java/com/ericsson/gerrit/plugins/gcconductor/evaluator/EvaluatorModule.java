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

package com.ericsson.gerrit.plugins.gcconductor.evaluator;

import com.ericsson.gerrit.plugins.gcconductor.CommonModule;
import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.validators.UploadValidationListener;
import com.google.inject.Provides;
import com.google.inject.internal.UniqueAnnotations;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.eclipse.jgit.transport.PostUploadHook;

/** Configures bindings of the evaluator. */
public class EvaluatorModule extends LifecycleModule {
  @Override
  protected void configure() {
    install(new CommonModule(EvaluatorConfig.class));
    listener().to(OnPluginLoadUnload.class);

    bind(ScheduledThreadPoolExecutor.class)
        .annotatedWith(EvaluatorExecutor.class)
        .toProvider(EvaluatorExecutorProvider.class);
    bind(ShutdownListener.class)
        .annotatedWith(UniqueAnnotations.create())
        .to(EvaluatorExecutorProvider.class);

    bind(Evaluator.class);
    DynamicSet.bind(binder(), UploadValidationListener.class).to(Evaluator.class);
    DynamicSet.bind(binder(), PostUploadHook.class).to(Evaluator.class);
    DynamicSet.bind(binder(), GitReferenceUpdatedListener.class).to(Evaluator.class);
    bind(EvaluatorConfig.class);
  }

  @Provides
  PluginConfig providePluginConfig(PluginConfigFactory config, @PluginName String pluginName) {
    return config.getFromGerritConfig(pluginName, true);
  }
}
