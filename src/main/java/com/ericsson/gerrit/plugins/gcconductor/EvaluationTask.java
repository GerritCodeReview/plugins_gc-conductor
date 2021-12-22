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

package com.ericsson.gerrit.plugins.gcconductor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.GC;
import org.eclipse.jgit.internal.storage.file.GC.RepoStatistics;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.ObjectWalk;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Evaluate the dirtiness of a repository. */
public class EvaluationTask implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(EvaluationTask.class);

  private final CommonConfig cfg;
  private final GcQueue queue;
  private final String hostname;

  private String repositoryPath;

  public interface Factory {
    /**
     * Instantiates EvaluationTask objects.
     *
     * @param repositoryPath path to the repository to consider.
     * @return an instance of EvaluationTask.
     */
    EvaluationTask create(String repositoryPath);
  }

  /**
   * Creates an EvaluationTask object.
   *
   * @param cfg The configuration where to read from dirtiness settings
   * @param queue The queue to add the repository to be garbage collected
   * @param hostname The hostname where the repository is evaluated.
   * @param repositoryPath Path to the repository to evaluate.
   */
  @Inject
  public EvaluationTask(
      CommonConfig cfg, GcQueue queue, @Hostname String hostname, @Assisted String repositoryPath) {
    this.cfg = cfg;
    this.queue = queue;
    this.hostname = hostname;
    this.repositoryPath = repositoryPath;
  }

  @Override
  public void run() {
    if (!isAlreadyInQueue() && isDirty()) {
      insertRepository();
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(repositoryPath);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EvaluationTask)) {
      return false;
    }
    EvaluationTask other = (EvaluationTask) obj;
    return repositoryPath.equals(other.repositoryPath);
  }

  private boolean isAlreadyInQueue() {
    try {
      return queue.contains(repositoryPath);
    } catch (GcQueueException e) {
      log.error("Error checking if repository is already in queue {}", repositoryPath, e);
      return true;
    }
  }

  private boolean isDirty() {
    try (FileRepository repository =
        (FileRepository)
            RepositoryCache.open(FileKey.exact(new File(repositoryPath), FS.DETECTED))) {
      RepoStatistics statistics = new GC(repository).getStatistics();
      if (statistics.numberOfPackFiles >= cfg.getPackedThreshold()) {
        log.debug(
            "The number of packs ({}) exceeds the configured limit of {}",
            statistics.numberOfPackFiles,
            cfg.getPackedThreshold());
        return true;
      }
      long looseObjects = statistics.numberOfLooseObjects;
      int looseThreshold = cfg.getLooseThreshold();
      if (looseObjects >= looseThreshold) {
        long referencedLooseObjects = 0;
        long unreferencedLooseObjects = 0;
        long duration = 0;
        long start = System.currentTimeMillis();
        unreferencedLooseObjects = getUnreferencedLooseObjectsCount(repository);
        duration = System.currentTimeMillis() - start;
        referencedLooseObjects = looseObjects - unreferencedLooseObjects;
        log.debug(
            "{} of {} loose objects in repository {} were unreferenced. Evaluating unreferenced objects took {}ms.",
            unreferencedLooseObjects,
            looseObjects,
            repositoryPath,
            duration);
        return referencedLooseObjects >= looseThreshold;
      }
    } catch (RepositoryNotFoundException rnfe) {
      log.debug("Repository no longer exist, aborting evaluation.");
    } catch (IOException e) {
      log.error("Error gathering '{}' statistics.", repositoryPath, e);
    }
    return false;
  }

  @VisibleForTesting
  int getUnreferencedLooseObjectsCount(FileRepository repo) throws IOException {
    File objects = repo.getObjectsDirectory();
    String[] fanout = objects.list();
    if (fanout == null || fanout.length == 0) {
      return 0;
    }
    Set<ObjectId> unreferencedCandidates = getUnreferencedCandidates(objects, fanout);
    if (unreferencedCandidates.isEmpty()) {
      return 0;
    }
    try (ObjectWalk walk = new ObjectWalk(repo)) {
      for (Ref ref : getAllRefs(repo)) {
        walk.markStart(walk.parseAny(ref.getObjectId()));
      }
      removeReferenced(unreferencedCandidates, walk);
    }
    return unreferencedCandidates.size();
  }

  private Set<ObjectId> getUnreferencedCandidates(File objects, String[] fanout) {
    Set<ObjectId> candidates = new HashSet<>();
    for (String dir : fanout) {
      if (dir.length() != 2) {
        continue;
      }
      File[] entries = new File(objects, dir).listFiles();
      if (entries != null) {
        addCandidates(candidates, dir, entries);
      }
    }
    return candidates;
  }

  private void addCandidates(Set<ObjectId> candidates, String dir, File[] entries) {
    for (File f : entries) {
      String fileName = f.getName();
      if (fileName.length() != Constants.OBJECT_ID_STRING_LENGTH - 2) {
        continue;
      }
      try {
        ObjectId id = ObjectId.fromString(dir + fileName);
        candidates.add(id);
      } catch (IllegalArgumentException notAnObject) {
        // ignoring the file that does not represent loose object
      }
    }
  }

  private Collection<Ref> getAllRefs(FileRepository repo) throws IOException {
    RefDatabase refdb = repo.getRefDatabase();
    Collection<Ref> refs = refdb.getRefs();
    List<Ref> addl = refdb.getAdditionalRefs();
    if (!addl.isEmpty()) {
      List<Ref> all = new ArrayList<>(refs.size() + addl.size());
      all.addAll(refs);
      // add additional refs which start with refs/
      for (Ref r : addl) {
        if (r.getName().startsWith(Constants.R_REFS)) {
          all.add(r);
        }
      }
      return all;
    }
    return refs;
  }

  private void removeReferenced(Set<ObjectId> id2File, ObjectWalk w) throws IOException {
    RevObject ro = w.next();
    while (ro != null) {
      if (id2File.remove(ro.getId()) && id2File.isEmpty()) {
        return;
      }
      ro = w.next();
    }
    ro = w.nextObject();
    while (ro != null) {
      if (id2File.remove(ro.getId()) && id2File.isEmpty()) {
        return;
      }
      ro = w.nextObject();
    }
  }

  private void insertRepository() {
    try {
      queue.add(repositoryPath, hostname, false); //TODO: determine default isAggressive
    } catch (GcQueueException e) {
      log.error("Error adding repository in queue {}", repositoryPath, e);
    }
  }

  @Override
  public String toString() {
    return "Evaluate if repository need GC: " + repositoryPath;
  }
}
