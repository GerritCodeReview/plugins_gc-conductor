// Copyright (C) 2016 The Android Open Source Project
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask;
import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask.Factory;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EvaluatorTest {
  private static final String REPOSITORY_PATH = "/path/someRepo.git";
  private static final Project.NameKey NAME_KEY = new Project.NameKey("testProject");

  @Mock private EvaluationTask task;
  @Mock private GitReferenceUpdatedListener.Event event;
  @Mock private GitRepositoryManager repoManager;
  @Mock private Repository repository;
  @Mock private ScheduledThreadPoolExecutor executor;
  @Mock private EvaluatorConfig config;
  @Mock private Config gerritConfig;

  private Evaluator evaluator;

  @Before
  public void createEvaluator() {
    when(event.getProjectName()).thenReturn(NAME_KEY.get());
    task = new EvaluationTask(null, null, null, REPOSITORY_PATH);
    when(repository.getDirectory()).thenReturn(new File(REPOSITORY_PATH));
    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH)).thenReturn(task);
    when(config.getExpireTimeRecheck()).thenReturn(0L);
    when(gerritConfig.getInt(
            "receive", null, "threadPoolSize", Runtime.getRuntime().availableProcessors()))
        .thenReturn(1);
    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
  }

  @Test
  public void onPostUploadShouldCreateTaskOnlyIfPreUploadCalled() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor).execute(task);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskIfRepositoryIsNull() {
    File fileMock = mock(File.class);
    when(fileMock.getAbsolutePath()).thenReturn(null);
    when(repository.getDirectory()).thenReturn(fileMock);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, never()).execute(task);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskRepoIsNull() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).execute(task);
  }

  @Test
  public void onPostUploadShouldCreateTaskExpired() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(2)).execute(task);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskNotExpired() {
    when(config.getExpireTimeRecheck()).thenReturn(1000L);
    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH)).thenReturn(task);
    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).execute(task);
  }

  @Test
  public void onGitReferenceUpdatedShouldCreateTaskExpired() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repository);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(2)).execute(task);
  }

  @Test
  public void onGitReferenceUpdatedShouldNotCreateTaskNotExpired() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repository);
    when(config.getExpireTimeRecheck()).thenReturn(1000L);
    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH)).thenReturn(task);
    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(1)).execute(task);
  }

  @Test
  public void onGitReferenceUpdatedThrowsRepositoryNotFoundException() throws Exception {
    doThrow(new RepositoryNotFoundException("")).when(repoManager).openRepository(NAME_KEY);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, never()).execute(task);
  }

  @Test
  public void onGitReferenceUpdatedThrowsIOException() throws Exception {
    doThrow(new IOException()).when(repoManager).openRepository(NAME_KEY);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, never()).execute(task);
  }
}
