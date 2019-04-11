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

import com.ericsson.gerrit.plugins.gcconductor.EvaluationTask;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.validators.UploadValidationListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.pack.PackStatistics;
import org.eclipse.jgit.transport.PostUploadHook;
import org.eclipse.jgit.transport.UploadPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class Evaluator implements UploadValidationListener, PostUploadHook, GitReferenceUpdatedListener {
  private static final Logger log = LoggerFactory.getLogger(Evaluator.class);
  private static final ThreadLocal<String> uploadRepositoryPath = new ThreadLocal<String>() {};

  private final ScheduledThreadPoolExecutor executor;
  private final EvaluationTask.Factory evaluationTaskFactory;
  private final GitRepositoryManager repoManager;
  private final Map<String, Long> timestamps;
  private final Set<EvaluationTask> queuedEvaluationTasks =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  private long expireTime;

  @Inject
  Evaluator(
      @EvaluatorExecutor ScheduledThreadPoolExecutor executor,
      EvaluationTask.Factory evaluationTaskFactory,
      GitRepositoryManager repoManager,
      EvaluatorConfig config,
      @GerritServerConfig Config gerritConfig) {
    this.executor = executor;
    this.evaluationTaskFactory = evaluationTaskFactory;
    this.repoManager = repoManager;
    this.expireTime = config.getExpireTimeRecheck();

    int threads =
        gerritConfig.getInt(
            "receive", null, "threadPoolSize", Runtime.getRuntime().availableProcessors());
    timestamps = new ConcurrentHashMap<>(10000, 0.75f, threads);
  }

  @Override
  public void onPreUpload(
      Repository repository,
      Project project,
      String remoteHost,
      UploadPack up,
      Collection<? extends ObjectId> wants,
      Collection<? extends ObjectId> haves) {
    uploadRepositoryPath.set(repository.getDirectory().getAbsolutePath());
  }

  @Override
  public void onBeginNegotiate(
      Repository repository,
      Project project,
      String remoteHost,
      UploadPack up,
      Collection<? extends ObjectId> wants,
      int cntOffered) {
    // Do nothing
  }

  @Override
  public void onPostUpload(PackStatistics stats) {
    String repositoryPath = uploadRepositoryPath.get();
    if (repositoryPath != null) {
      queueEvaluationIfNecessary(repositoryPath);
      uploadRepositoryPath.remove();
    }
  }

  @Override
  public void onGitReferenceUpdated(Event event) {
    String projectName = event.getProjectName();
    Project.NameKey projectNameKey = new Project.NameKey(projectName);
    try (Repository repository = repoManager.openRepository(projectNameKey)) {
      String repositoryPath = repository.getDirectory().getAbsolutePath();
      queueEvaluationIfNecessary(repositoryPath);
    } catch (RepositoryNotFoundException e) {
      log.error("Project not found {}", projectName, e);
    } catch (IOException e) {
      log.error("Error getting repository for project {}", projectName, e);
    }
  }

  private void queueEvaluationIfNecessary(String repositoryPath) {
    if (lastCheckExpired(repositoryPath)) {
      EvaluationTask evaluationTask = evaluationTaskFactory.create(repositoryPath);
      if (queuedEvaluationTasks.add(evaluationTask)) {
        Future future = executor.submit(evaluationTask);
        addTaskListener(future, evaluationTask);
        timestamps.put(repositoryPath, System.currentTimeMillis());
      }
    }
  }

  private void addTaskListener(Future future, EvaluationTask evaluationTask) {
    ListenableFuture listenableFuture = JdkFutureAdapters.listenInPoolThread(future);
    listenableFuture.addListener(
        new Runnable() {
          public void run() {
            queuedEvaluationTasks.remove(evaluationTask);
          }
        },
        MoreExecutors.directExecutor());
  }

  private boolean lastCheckExpired(String repositoryPath) {
    return !timestamps.containsKey(repositoryPath)
        || System.currentTimeMillis() >= timestamps.get(repositoryPath) + expireTime;
  }
}
