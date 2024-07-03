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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledEvaluatorTest {

  @Mock private ScheduledEvaluationTask evaluationTask;

  private ScheduledEvaluator scheduledEvaluator;

  @Test
  public void testScheduleWith() throws Exception {
    scheduledEvaluator = new ScheduledEvaluator(evaluationTask);
    scheduledEvaluator.scheduleWith(1, 2000);
    TimeUnit.MILLISECONDS.sleep(100);
    verify(evaluationTask).run();
    scheduledEvaluator.onShutdown();
  }

  @Test
  public void testOnShutdown() {
    scheduledEvaluator = new ScheduledEvaluator(evaluationTask);
    scheduledEvaluator.onShutdown();
    verifyNoInteractions(evaluationTask);
  }
}
