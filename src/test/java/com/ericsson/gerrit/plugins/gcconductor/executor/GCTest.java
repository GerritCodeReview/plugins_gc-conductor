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

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.internal.storage.file.GC.RepoStatistics;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GCTest {

  @Rule public TemporaryFolder dir = new TemporaryFolder();

  @Test
  public void testGc() throws Exception {
    Repository repository = createRepository("repoTest");
    RepoStatistics stats = computeFor(repository);
    assertThat(stats.numberOfLooseObjects).isEqualTo(0);

    addFileTo(repository);
    stats = computeFor(repository);
    assertThat(stats.numberOfLooseObjects).isEqualTo(2);
    ProgressMonitor cpm = new CancellableProgressMonitor();
    GarbageCollector gc = new GarbageCollector();
    gc.setRepositoryPath(repository.getDirectory().getAbsolutePath());
    gc.setPm(cpm);

    assertThat(gc.call()).isTrue();
    stats = computeFor(repository);
    assertThat(stats.numberOfLooseObjects).isEqualTo(0);
    assertThat(stats.numberOfPackedRefs).isEqualTo(1);
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

  private RepoStatistics computeFor(Repository repository) throws IOException {
    return new GC((FileRepository) repository).getStatistics();
  }
}
