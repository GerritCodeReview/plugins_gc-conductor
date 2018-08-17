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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;

class GarbageCollector implements Callable<Boolean> {
  private String repositoryPath;
  private ProgressMonitor pm;

  void setRepositoryPath(String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  void setPm(ProgressMonitor pm) {
    this.pm = pm;
  }

  @Override
  public Boolean call() throws Exception {
    try (Git git = Git.open(new File(repositoryPath))) {
      git.gc()
          .setAggressive(true)
          .setPreserveOldPacks(true)
          .setPrunePreserved(true)
          .setProgressMonitor(pm)
          .call();
      return true;
    } catch (GitAPIException e) {
      throw new IOException(e);
    }
  }
}
