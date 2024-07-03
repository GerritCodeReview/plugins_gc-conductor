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

import static com.ericsson.gerrit.plugins.gcconductor.executor.GcExecutor.CONFIG_FILE_PROPERTY;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GcExecutorTest {

  private static final String HOSTNAME = "hostname";
  private static final String EXECUTOR = "hostname-0";
  private static final String REPOSITORY = "repository";

  @Mock private ExecutorConfig config;
  @Mock GcWorker.Factory gcWorkerFactory;
  @Mock private GcWorker gcWorker;
  @Mock private GcQueue gcQueue;
  @Mock private ScheduledEvaluator scheduledEvaluator;

  @Rule public TemporaryFolder testTempFolder = new TemporaryFolder();

  @Test
  public void testGcExecutor() {
    when(config.getExecutors()).thenReturn(1);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcWorker).start();
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testLeftOverReposAreUnpickedWhenStarting() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    when(gcQueue.list())
        .thenReturn(
            ImmutableList.of(new RepositoryInfo(REPOSITORY, null, EXECUTOR, HOSTNAME, true)));
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcQueue).unpick(REPOSITORY);
    verify(gcWorker).start();
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testUnassignedReposAreNotUnpickedWhenStarting() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    when(gcQueue.list())
        .thenReturn(ImmutableList.of(new RepositoryInfo(REPOSITORY, null, null, HOSTNAME, true)));
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcQueue, never()).unpick(REPOSITORY);
    verify(gcWorker).start();
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testExecutorDoesNotUnpickNotOwnedRepo() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    when(gcQueue.list())
        .thenReturn(
            ImmutableList.of(
                new RepositoryInfo(
                    REPOSITORY, null, "another executor", "another hostname", true)));
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcQueue, never()).unpick(REPOSITORY);
    verify(gcWorker).start();
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testQueueThrowsExceptionUnpickingAtStart() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    doThrow(new GcQueueException("", new Throwable())).when(gcQueue).list();
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcQueue, never()).unpick(REPOSITORY);
    verify(gcWorker).start();
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testScheduledEvaluationIsConfigured() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(config.getInitialDelay()).thenReturn(1L);
    when(config.getInterval()).thenReturn(1L);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    when(gcQueue.list())
        .thenReturn(ImmutableList.of(new RepositoryInfo(REPOSITORY, null, null, HOSTNAME, true)));
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcWorker).start();
    verify(scheduledEvaluator).scheduleWith(1L, 1L);
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testScheduledEvaluationBothParametersRequired() throws Exception {
    when(config.getExecutors()).thenReturn(1);
    when(config.getInitialDelay()).thenReturn(1L);
    when(gcWorkerFactory.create(EXECUTOR)).thenReturn(gcWorker);
    when(gcQueue.list())
        .thenReturn(ImmutableList.of(new RepositoryInfo(REPOSITORY, null, null, HOSTNAME, true)));
    GcExecutor gcExecutor =
        new GcExecutor(gcQueue, config, gcWorkerFactory, scheduledEvaluator, HOSTNAME);
    verify(gcWorker).start();
    verifyNoInteractions(scheduledEvaluator);
    gcExecutor.onShutdown();
    verify(gcWorker).shutdown();
  }

  @Test
  public void testloadConfigWhenNotSpecified() {
    Config config = GcExecutor.loadConfig();
    assertThat(config.toText()).isEmpty();
  }

  @Test
  public void testloadConfigFromWhenSpecifiedByProperty() throws Exception {
    File configFile = testTempFolder.newFile();
    FileBasedConfig specifiedConfig = new FileBasedConfig(configFile, FS.DETECTED);
    specifiedConfig.setString("otherSection", null, "otherKey", "otherValue");
    specifiedConfig.save();
    System.setProperty(CONFIG_FILE_PROPERTY, configFile.getAbsolutePath());
    Config config = GcExecutor.loadConfig();
    assertThat(config.toText()).isEqualTo(specifiedConfig.toText());
    System.clearProperty(CONFIG_FILE_PROPERTY);
  }

  @Test
  public void shouldReturnEmptyConfigIfAnErrorOccur() throws Exception {
    File file = testTempFolder.newFile();
    Files.write(file.toPath(), "[section]\n invalid!@$@#%#$".getBytes(), StandardOpenOption.CREATE);
    System.setProperty(CONFIG_FILE_PROPERTY, file.getAbsolutePath());
    Config config = GcExecutor.loadConfig();
    assertThat(config.toText()).isEmpty();
    System.clearProperty(CONFIG_FILE_PROPERTY);
  }
}
