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

package com.ericsson.gerrit.plugins.gcconductor;

import com.ericsson.gerrit.plugins.gcconductor.postgresqueue.PostgresModule;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Module containing common bindings to evaluator and executor */
public class CommonModule extends AbstractModule {

  private Class<? extends CommonConfig> commonConfig;

  public CommonModule(Class<? extends CommonConfig> commonConfig) {
    this.commonConfig = commonConfig;
  }

  @Override
  protected void configure() {
    install(new PostgresModule(commonConfig));
    install(new FactoryModuleBuilder().build(EvaluationTask.Factory.class));
  }

  @Provides
  @Singleton
  @Hostname
  String provideHostname() throws Exception {
    ProcessBuilder builder = new ProcessBuilder("hostname", "-s");
    builder.redirectErrorStream(true);
    Process process = builder.start();
    process.waitFor();
    try (BufferedReader buffer =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      return buffer.readLine();
    }
  }

  @Provides
  List<ShutdownListener> provideShutdownListeners(Injector injector) {
    List<ShutdownListener> listeners = new ArrayList<>();
    for (Binding<ShutdownListener> shutdownListenerBinding :
        injector.findBindingsByType(new TypeLiteral<ShutdownListener>() {})) {
      listeners.add(shutdownListenerBinding.getProvider().get());
    }
    return listeners;
  }
}
