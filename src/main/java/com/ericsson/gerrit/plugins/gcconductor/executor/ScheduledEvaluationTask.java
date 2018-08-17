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

import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask;
import com.google.inject.Inject;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class ScheduledEvaluationTask implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(ScheduledEvaluationTask.class);

  private final EvaluationTask.Factory evaluationTaskFactory;
  private final Path repositoriesPath;

  @Inject
  public ScheduledEvaluationTask(
      EvaluationTask.Factory evaluationTaskFactory, ExecutorConfig config) {
    this.evaluationTaskFactory = evaluationTaskFactory;
    try {
      repositoriesPath = Paths.get(config.getRepositoriesPath()).normalize().toRealPath();
    } catch (IOException e) {
      log.error("Failed to resolve repositoriesPath.", e);
      throw new ProvisionException("Failed to resolve repositoriesPath: " + e.getMessage());
    }
  }

  @Override
  public void run() {
    for (String repositoryPath : repositories()) {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }
      evaluationTaskFactory.create(repositoryPath).run();
    }
  }

  private Collection<String> repositories() {
    ProjectVisitor visitor = new ProjectVisitor(repositoriesPath);
    try {
      Files.walkFileTree(
          visitor.startFolder,
          EnumSet.of(FileVisitOption.FOLLOW_LINKS),
          Integer.MAX_VALUE,
          visitor);
    } catch (IOException e) {
      log.error("Error walking repository tree {}", visitor.startFolder.toAbsolutePath(), e);
    }
    return Collections.unmodifiableSortedSet(visitor.found);
  }

  class ProjectVisitor extends SimpleFileVisitor<Path> {
    private final SortedSet<String> found = new TreeSet<>();
    private Path startFolder;

    private ProjectVisitor(Path startFolder) {
      this.startFolder = startFolder;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      if (!dir.equals(startFolder) && isRepo(dir)) {
        found.add(dir.toString());
        return FileVisitResult.SKIP_SUBTREE;
      }
      return FileVisitResult.CONTINUE;
    }

    private boolean isRepo(Path p) {
      return FileKey.isGitRepository(p.toFile(), FS.DETECTED);
    }
  }
}
