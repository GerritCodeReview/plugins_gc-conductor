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

package com.ericsson.gerrit.plugins.gcconductor;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Queue that holds Git repositories to be Gc'ed.
 *
 * <p>Implementations of this interface should be registered in Guice as {@link Singleton}.
 */
public interface GcQueue {

  /**
   * Add repository to the queue.
   *
   * <p>Repositories are unique in the queue. This method is idempotent, adding an already existing
   * repository will neither add it again nor throw an exception.
   *
   * @param repository The path to the repository.
   * @param queuedFrom The hostname from which the repository is queued from.
   * @throws GcQueueException if an error occur while adding the repository.
   */
  void add(String repository, String queuedFrom, boolean isAggressive) throws GcQueueException;

  /**
   * Pick a repository from the queue.
   *
   * <p>If the queue contains an already picked repository by the specified executor, will return
   * that repository.
   *
   * @param executor The name of the executor to assign repository to.
   * @param queuedForLongerThan Only pick repository that were in the queue for longer than
   *     specified number of seconds.
   * @param queuedFrom If specified, only pick repository if queued from the specified hostname.
   * @return RepositoryInfo representing the repository if any, otherwise return <code>null</code>.
   * @throws GcQueueException if an error occur while picking a repository.
   */
  RepositoryInfo pick(String executor, long queuedForLongerThan, Optional<String> queuedFrom)
      throws GcQueueException;

  /**
   * Unpick a repository from the queue.
   *
   * @param repository The path to the repository to unpick.
   * @throws GcQueueException if an error occur while unpicking the repository.
   */
  void unpick(String repository) throws GcQueueException;

  /**
   * Remove a repository from the queue.
   *
   * @param repository The path to the repository to remove.
   * @throws GcQueueException if an error occur while removing the repository.
   */
  void remove(String repository) throws GcQueueException;

  /**
   * Returns <code>true</code> if the queue contains the specified repository.
   *
   * @param repository The path to the repository.
   * @return <code>true</code> if the queue contains the specified repository, otherwise <code>false
   *     </code>.
   * @throws GcQueueException if an error occur which checking if the repository is in the queue.
   */
  boolean contains(String repository) throws GcQueueException;

  /**
   * Reset all repositories queuedFrom to the specified hostname.
   *
   * @param queuedFrom The hostname to set queuedFrom to for every repository.
   * @throws GcQueueException if an error occur while resetting queuedFrom.
   */
  void resetQueuedFrom(String queuedFrom) throws GcQueueException;

  /**
   * Returns list of repositories with their queuedAt timestamp, queuedFrom hostname and their
   * associated executor, if any. The repositories are orderer from the oldest to the newest
   * inserted.
   *
   * @return list of RepositoryInfo.
   * @throws GcQueueException if an error occur while listing the repositories.
   */
  List<RepositoryInfo> list() throws GcQueueException;

  /**
   * Bump an existing repository to the top of the queue.
   *
   * @param repository The path to the repository.
   * @throws GcQueueException if an error occurs while bumping the sequence of a repository.
   */
  void bumpToFirst(String repository) throws GcQueueException;
}
