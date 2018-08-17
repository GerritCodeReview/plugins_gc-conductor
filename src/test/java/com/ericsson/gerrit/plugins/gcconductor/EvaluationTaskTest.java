// Copyright (C) 2016 Ericsson
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationTaskTest {

  @Rule public TemporaryFolder dir = new TemporaryFolder();

  private static final String SOME_HOSTNAME = "hostname";

  @Mock private EvaluatorConfig cfg;
  @Mock private GcQueue queue;

  private EvaluationTask task;
  private Repository repository;
  private String repositoryPath;

  @Before
  public void setUp() throws Exception {
    repository = createRepository("someRepo.git");
    repositoryPath = repository.getDirectory().getAbsolutePath();
    task = new EvaluationTask(cfg, queue, SOME_HOSTNAME, repositoryPath);
  }

  @Test
  public void dirtyRepositoryObjectsShouldBeAddedToTheQueue() throws Exception {
    when(cfg.getPackedThreshold()).thenReturn(1);
    addFileTo(repository);
    when(queue.contains(repositoryPath)).thenReturn(false);
    task = new EvaluationTask(cfg, queue, SOME_HOSTNAME, repositoryPath);
    task.run();
    verify(queue).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void dirtyRepositoryPacksShouldBeAddedToTheQueue() throws Exception {
    when(cfg.getPackedThreshold()).thenReturn(1);
    when(queue.contains(repositoryPath)).thenReturn(false);
    addFileTo(repository);
    gc(repository);
    task = new EvaluationTask(cfg, queue, SOME_HOSTNAME, repositoryPath);
    task.run();
    verify(queue).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void repositoryShouldNotBeAddedIfAlreadyInQueue() throws Exception {
    when(queue.contains(repositoryPath)).thenReturn(true);
    task.run();
    verify(queue, never()).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void cleanRepositoryShouldNotBeAddedToQueue() throws Exception {
    when(cfg.getLooseThreshold()).thenReturn(1);
    when(cfg.getPackedThreshold()).thenReturn(1);
    when(queue.contains(repositoryPath)).thenReturn(false);
    task.run();
    verify(queue, never()).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void queueThrowsErrorCheckingIfRepositoryExists() throws Exception {
    doThrow(new GcQueueException("some message", new Throwable()))
        .when(queue)
        .contains(repositoryPath);
    task.run();
    verify(queue, never()).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void queueThrowsErrorInsertingRepository() throws Exception {
    when(queue.contains(repositoryPath)).thenReturn(false);
    doThrow(new GcQueueException("some message", new Throwable()))
        .when(queue)
        .add(repositoryPath, SOME_HOSTNAME);
    task.run();
  }

  @Test
  public void repositoryNoLongerExist() throws Exception {
    when(queue.contains(repositoryPath)).thenReturn(false);
    dir.delete();
    task.run();
    verify(queue, never()).add(repositoryPath, SOME_HOSTNAME);
  }

  @Test
  public void toStringReturnsProperMessage() {
    assertThat(task.toString()).isEqualTo("Evaluate if repository need GC: " + repositoryPath);
  }

  @Test
  public void noUnreferencedObjects() throws Exception {
    addFileTo(repository);
    FileRepository fileRepository = (FileRepository) repository;
    assertThat(task.getUnreferencedLooseObjectsCount(fileRepository)).isEqualTo(0);
  }

  @Test
  public void unreferencedObjectsCountShouldBeOne() throws Exception {
    try (Git git = new Git(repository)) {
      RevCommit initialCommit = git.commit().setMessage("initial commit").call();
      addFileTo(repository);
      git.reset().setMode(ResetType.HARD).setRef(initialCommit.getName()).call();
    }
    FileRepository fileRepository = (FileRepository) repository;
    removeReflog(fileRepository.getDirectory());
    assertThat(task.getUnreferencedLooseObjectsCount(fileRepository)).isEqualTo(1);
  }

  private void removeReflog(File directory) throws IOException {
    Path logs = directory.toPath().resolve("logs");
    Files.walk(logs).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  private Repository createRepository(String repoName) throws Exception {
    File repo = dir.newFolder(repoName);
    try (Git git = Git.init().setDirectory(repo).call()) {
      return git.getRepository();
    }
  }

  private void addFileTo(Repository repository) throws Exception {
    try (Git git = new Git(repository)) {
      new File(repository.getDirectory().getParent(), "test");
      git.add().addFilepattern("test").call();
      git.commit().setMessage("Add test file").call();
    }
  }

  private void gc(Repository repository) throws Exception {
    try (Git git = new Git(repository)) {
      git.gc().setAggressive(true).call();
    }
  }
}
