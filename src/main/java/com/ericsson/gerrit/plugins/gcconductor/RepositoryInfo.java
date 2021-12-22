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

import java.sql.Timestamp;

/** Information about a queued repository. */
public class RepositoryInfo {

  private final String path;
  private final Timestamp queuedAt;
  private final String executor;
  private final String queuedFrom;
  private final boolean aggressive;

  public RepositoryInfo(String path, Timestamp queuedAt, String executor, String queuedFrom,
      boolean aggressive) {
    this.path = path;
    this.queuedAt = queuedAt;
    this.executor = executor;
    this.queuedFrom = queuedFrom;
    this.aggressive = aggressive;
  }

  /** @return the path to the repository. */
  public String getPath() {
    return path;
  }

  /** @return the time the repository was queued at. */
  public Timestamp getQueuedAt() {
    return queuedAt;
  }

  /** @return the executor running gc on the repository or <code>null</code> if none. */
  public String getExecutor() {
    return executor;
  }

  /** @return the hostname that inserted the repository in the queue. */
  public String getQueuedFrom() {
    return queuedFrom;
  }

  public boolean isAggressive() {
    return aggressive;
  }

}
