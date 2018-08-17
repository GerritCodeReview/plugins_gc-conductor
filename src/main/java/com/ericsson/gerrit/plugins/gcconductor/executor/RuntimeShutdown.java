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

import com.ericsson.gerrit.plugins.gcconductor.ShutdownNotifier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class RuntimeShutdown {
  private static final Logger log = LoggerFactory.getLogger(RuntimeShutdown.class);

  private final ShutdownHook hook;
  private final ShutdownNotifier shutdownNotifier;

  @Inject
  RuntimeShutdown(ShutdownNotifier shutdownNotifier) {
    this.shutdownNotifier = shutdownNotifier;
    hook = new ShutdownHook();
    Runtime.getRuntime().addShutdownHook(hook);
  }

  public void waitFor() {
    hook.waitForShutdown();
  }

  private class ShutdownHook extends Thread {
    private boolean completed;

    ShutdownHook() {
      setName("ShutdownCallback");
    }

    @Override
    public void run() {
      log.debug("Graceful shutdown requested");
      shutdownNotifier.notifyAllListeners();
      log.debug("Shutdown complete");
      synchronized (this) {
        completed = true;
        notifyAll();
      }
    }

    void waitForShutdown() {
      synchronized (this) {
        while (!completed) {
          try {
            wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }
  }
}
