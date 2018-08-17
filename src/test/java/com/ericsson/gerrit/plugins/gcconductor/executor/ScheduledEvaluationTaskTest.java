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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask;
import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask.Factory;
import com.google.inject.ProvisionException;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledEvaluationTaskTest {

  @Rule public TemporaryFolder dir = new TemporaryFolder();

  @Mock private ExecutorConfig config;
  @Mock private Factory evaluationTaskFactory;
  @Mock private EvaluationTask evaluationTask;

  private ScheduledEvaluationTask periodicEvaluator;

  @Before
  public void setUp() {
    when(config.getRepositoriesPath()).thenReturn(dir.getRoot().getAbsolutePath());
    periodicEvaluator = new ScheduledEvaluationTask(evaluationTaskFactory, config);
  }

  @Test(expected = ProvisionException.class)
  public void shouldThrowAnExceptionOnInvalidrepositoryPath() {
    when(config.getRepositoriesPath()).thenReturn("unexistingPath");
    periodicEvaluator = new ScheduledEvaluationTask(evaluationTaskFactory, config);
  }

  @Test
  public void shouldAddRepository() throws Exception {
    Repository repository = createRepository("repoTest");
    when(evaluationTaskFactory.create(repository.getDirectory().getAbsolutePath()))
        .thenReturn(evaluationTask);
    periodicEvaluator.run();
    verify(evaluationTask).run();
  }

  @Test
  public void shouldNotAddFolderIfNotRepository() throws Exception {
    File notARepo = dir.newFolder("notRepository");
    periodicEvaluator.run();
    verify(evaluationTaskFactory, never()).create(notARepo.getAbsolutePath());
  }

  @Test
  public void shouldHonorInterruption() throws Exception {
    createRepository("repoTest");
    Thread t = new Thread(() -> periodicEvaluator.run());
    t.start();
    t.interrupt();
    verifyZeroInteractions(evaluationTaskFactory);
  }

  private Repository createRepository(String repoName) throws Exception {
    File repo = dir.newFolder(repoName);
    try (Git git = Git.init().setDirectory(repo).call()) {
      return git.getRepository();
    }
  }
}
