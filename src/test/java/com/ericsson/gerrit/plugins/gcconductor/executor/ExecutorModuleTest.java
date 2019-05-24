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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.when;

import com.google.inject.Binder;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecutorModuleTest {
  @Mock private Binder binder;
  @Mock private ExecutorConfig execCfg;
  @Mock private Config config;

  private ExecutorModule executorModule;

  @Before
  public void setUp() {
    executorModule = new ExecutorModule(config);
  }

  @Test
  public void testWasQueuedFrom() {
    String host = "hostname";
    when(execCfg.isPickOwnHostOnly()).thenReturn(true);
    Optional<String> wasQueuedFrom = executorModule.wasQueuedFrom(execCfg, host);
    assertThat(wasQueuedFrom).isPresent();
    assertThat(wasQueuedFrom.get()).isEqualTo(host);

    when(execCfg.isPickOwnHostOnly()).thenReturn(false);
    wasQueuedFrom = executorModule.wasQueuedFrom(execCfg, host);
    assertThat(wasQueuedFrom.isPresent()).isFalse();
  }

  @Test
  public void testQueuedForLongerThan() {
    when(execCfg.getDelay()).thenReturn(5);
    assertThat(executorModule.queuedForLongerThan(execCfg)).isEqualTo(5);
  }
}
