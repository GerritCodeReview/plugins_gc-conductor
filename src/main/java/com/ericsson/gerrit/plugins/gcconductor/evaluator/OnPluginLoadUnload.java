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

package com.ericsson.gerrit.plugins.gcconductor.evaluator;

import com.ericsson.gerrit.plugins.gcconductor.ShutdownNotifier;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.inject.Inject;

class OnPluginLoadUnload implements LifecycleListener {

  private final ShutdownNotifier shutdownNotifier;

  @Inject
  OnPluginLoadUnload(ShutdownNotifier shutdownNotifier) {
    this.shutdownNotifier = shutdownNotifier;
  }

  @Override
  public void start() {
    // nothing to do, only stop method is needed for now
  }

  @Override
  public void stop() {
    shutdownNotifier.notifyAllListeners();
  }
}
