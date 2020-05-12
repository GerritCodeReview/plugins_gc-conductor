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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
  private static final String REPOSITORY_PATH_OTHER = "/path/otherRepo.git";
  private static final Project.NameKey NAME_KEY = new Project.NameKey("testProject");

  @Mock private GitReferenceUpdatedListener.Event event;
  @Mock private GitRepositoryManager repoManager;
  @Mock private Repository repository;
  @Mock private Repository repositoryOther;
  @Mock private ExecutorService executor;
  @Mock private EvaluatorConfig config;
  @Mock private Config gerritConfig;

  private Evaluator evaluator;
  private EvaluationTask taskSamePathCompleted;
  private EvaluationTask taskSamePathNotCompleted;
  private EvaluationTask taskDifferentPath;

  @Before
  public void createEvaluator() {
    when(event.getProjectName()).thenReturn(NAME_KEY.get());

    when(config.getExpireTimeRecheck()).thenReturn(0L);
    when(gerritConfig.getInt(
            "receive", null, "threadPoolSize", Runtime.getRuntime().availableProcessors()))
        .thenReturn(1);

    when(repository.getDirectory()).thenReturn(new File(REPOSITORY_PATH));
    when(repositoryOther.getDirectory()).thenReturn(new File(REPOSITORY_PATH_OTHER));

    taskSamePathCompleted = new EvaluationTask(null, null, null, REPOSITORY_PATH);
    taskSamePathNotCompleted = new EvaluationTask(null, null, null, REPOSITORY_PATH);
    taskDifferentPath = new EvaluationTask(null, null, null, REPOSITORY_PATH_OTHER);

    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH))
        .thenReturn(taskSamePathNotCompleted)
        .thenReturn(taskSamePathCompleted);
    when(eventTaskFactory.create(REPOSITORY_PATH_OTHER)).thenReturn(taskDifferentPath);

    when(executor.submit(taskSamePathCompleted))
        .thenReturn(CompletableFuture.completedFuture(null));
    when(executor.submit(taskSamePathNotCompleted)).thenReturn(new CompletableFuture<>());
    when(executor.submit(taskDifferentPath)).thenReturn(CompletableFuture.completedFuture(null));

    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
  }

  @Test
  public void onPostUploadShouldCreateTaskOnlyIfPreUploadCalled() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor).submit(taskSamePathCompleted);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskIfRepositoryIsNull() {
    File fileMock = mock(File.class);
    when(fileMock.getAbsolutePath()).thenReturn(null);
    when(repository.getDirectory()).thenReturn(fileMock);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, never()).submit(taskSamePathCompleted);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskRepoIsNull() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).submit(taskSamePathCompleted);
  }

  @Test
  public void onPostUploadShouldCreateTaskExpired() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repositoryOther, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).submit(taskSamePathCompleted);
    verify(executor, times(1)).submit(taskDifferentPath);
  }

  @Test
  public void onPostUploadSameRepoShouldCreateSingleTaskOnly() {
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).submit(taskSamePathCompleted);
  }

  @Test
  public void onPostUploadCompletedTasksAreRemovedFromQueue() {
    evaluator.onPreUpload(repositoryOther, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repositoryOther, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(2)).submit(taskDifferentPath);
  }

  @Test
  public void onPostUploadShouldNotCreateTaskNotExpired() {
    when(config.getExpireTimeRecheck()).thenReturn(1000L);
    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH)).thenReturn(taskSamePathCompleted);
    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    evaluator.onPreUpload(repository, null, null, null, null, null);
    evaluator.onPostUpload(null);
    verify(executor, times(1)).submit(taskSamePathCompleted);
  }

  @Test
  public void onGitReferenceUpdatedShouldCreateTaskExpired() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repository).thenReturn(repositoryOther);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(1)).submit(taskSamePathCompleted);
    verify(executor, times(1)).submit(taskDifferentPath);
  }

  @Test
  public void onGitReferenceUpdatedSameRepoShouldCreateSingleTaskOnly() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repository);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(1)).submit(taskSamePathCompleted);
  }

  @Test
  public void onGitReferenceUpdatedCompletedTasksAreRemovedFromQueue() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repositoryOther);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(2)).submit(taskDifferentPath);
  }

  @Test
  public void onGitReferenceUpdatedShouldNotCreateTaskNotExpired() throws Exception {
    when(repoManager.openRepository(NAME_KEY)).thenReturn(repository);
    when(config.getExpireTimeRecheck()).thenReturn(1000L);
    Factory eventTaskFactory = mock(Factory.class);
    when(eventTaskFactory.create(REPOSITORY_PATH)).thenReturn(taskSamePathCompleted);
    evaluator = new Evaluator(executor, eventTaskFactory, repoManager, config, gerritConfig);
    evaluator.onGitReferenceUpdated(event);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, times(1)).submit(taskSamePathCompleted);
  }

  @Test
  public void onGitReferenceUpdatedThrowsRepositoryNotFoundException() throws Exception {
    doThrow(new RepositoryNotFoundException("")).when(repoManager).openRepository(NAME_KEY);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, never()).submit(taskSamePathCompleted);
  }

  @Test
  public void onGitReferenceUpdatedThrowsIOException() throws Exception {
    doThrow(new IOException()).when(repoManager).openRepository(NAME_KEY);
    evaluator.onGitReferenceUpdated(event);
    verify(executor, never()).submit(taskSamePathCompleted);
  }
}
