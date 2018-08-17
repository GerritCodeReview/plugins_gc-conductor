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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.junit.Test;

public class ShutdownNotifierTest {

  @Test
  public void testNolistener() {
    ShutdownNotifier notifier = new ShutdownNotifier(new ArrayList<>());
    notifier.notifyAllListeners();
  }

  @Test
  public void testWithlisteners() {
    ShutdownListener listenerMock = mock(ShutdownListener.class);
    ShutdownNotifier notifier = new ShutdownNotifier(Lists.newArrayList(listenerMock));
    notifier.notifyAllListeners();
    verify(listenerMock).onShutdown();
  }
}
