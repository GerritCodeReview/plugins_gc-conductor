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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GcWorkerTest {
  private static final String EXEC_NAME = Thread.currentThread().getName();
  private static final String REPO_PATH = "repo";
  private static final String HOSTNAME = "hostname";
  private static final Optional<String> QUEUED_FROM = Optional.empty();

  @Mock private GcQueue queue;
  @Mock private GarbageCollector garbageCollector;
  @Mock private CancellableProgressMonitor cpm;

  private DirectExecutor directExecutor;

  private RepositoryInfo repoInfo;

  private GcWorker gcTask;

  @Before
  public void setUp() {
    Thread.interrupted(); // reset the flag
    repoInfo = new RepositoryInfo(REPO_PATH, null, EXEC_NAME, HOSTNAME, true);
    gcTask = new GcWorker(queue, garbageCollector, cpm, QUEUED_FROM, 0, EXEC_NAME);
    directExecutor = new DirectExecutor();
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void shouldPickAndRemoveRepository() throws Exception {
    when(queue.pick(EXEC_NAME, 0, QUEUED_FROM)).thenReturn(repoInfo);
    when(cpm.isCancelled()).thenReturn(false).thenReturn(false).thenReturn(true);
    directExecutor.execute(gcTask);
    verify(garbageCollector).call();
    verify(queue).remove(REPO_PATH);
    cpm.cancel();
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void noRepositoryPicked() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(true);
    directExecutor.execute(gcTask);
    verifyNoInteractions(garbageCollector);
    verify(queue, never()).remove(any(String.class));
    verify(queue, never()).unpick(any(String.class));
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void interruptedWhenWaitingToPickRepository() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(true);
    Thread t = new Thread(() -> directExecutor.execute(gcTask));
    t.start();
    t.interrupt();
    verifyNoInteractions(garbageCollector);
    verify(queue, never()).remove(any(String.class));
    verify(queue, never()).unpick(any(String.class));
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void gcFailsOnRepository() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
    when(queue.pick(EXEC_NAME, 0, QUEUED_FROM)).thenReturn(repoInfo);
    doThrow(new IOException()).when(garbageCollector).call();
    directExecutor.execute(gcTask);
    verify(queue).remove(REPO_PATH);
    verify(queue, never()).unpick(any(String.class));
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void queueThrowsExceptionWhenPickingRepository() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(true);
    doThrow(new GcQueueException("", new Throwable())).when(queue).pick(EXEC_NAME, 0, QUEUED_FROM);
    directExecutor.execute(gcTask);
    verifyNoInteractions(garbageCollector);
    verify(queue, never()).remove(any(String.class));
    verify(queue, never()).unpick(any(String.class));
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void gcRepositoryIsInterrupted() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(true).thenReturn(true).thenReturn(true);
    when(queue.pick(EXEC_NAME, 0, QUEUED_FROM)).thenReturn(repoInfo);
    doThrow(new IOException()).when(garbageCollector).call();
    directExecutor.execute(gcTask);
    verify(queue).unpick(REPO_PATH);
    verify(queue, never()).remove(REPO_PATH);
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void queueThrowsExceptionWhenRemovingRepository() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(false).thenReturn(true);
    when(queue.pick(EXEC_NAME, 0, QUEUED_FROM)).thenReturn(repoInfo);
    doThrow(new GcQueueException("", new Throwable())).when(queue).remove(REPO_PATH);
    directExecutor.execute(gcTask);
    verify(garbageCollector).call();
    verify(queue).remove(REPO_PATH);
    verify(queue, never()).unpick(REPO_PATH);
  }

  @Test
  @SuppressWarnings("DoNotCall")
  public void queueThrowsExceptionWhenUnpickingRepository() throws Exception {
    when(cpm.isCancelled()).thenReturn(false).thenReturn(true).thenReturn(true).thenReturn(true);
    when(queue.pick(EXEC_NAME, 0, QUEUED_FROM)).thenReturn(repoInfo);
    doThrow(new IOException()).when(garbageCollector).call();
    doThrow(new GcQueueException("", new Throwable())).when(queue).unpick(REPO_PATH);
    directExecutor.execute(gcTask);
    verify(queue).unpick(REPO_PATH);
    verify(queue, never()).remove(REPO_PATH);
  }

  @Test
  public void callingShutdownSetsCancellableToTrue() {
    gcTask.shutdown();
    verify(cpm).cancel();
  }

  final class DirectExecutor implements Executor {
    public void execute(Runnable r) {
      r.run();
    }
  }
}
