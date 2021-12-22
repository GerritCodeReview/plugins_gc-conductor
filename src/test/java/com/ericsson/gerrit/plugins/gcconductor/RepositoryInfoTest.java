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

import static com.google.common.truth.Truth.assertThat;

import java.sql.Timestamp;
import org.junit.Test;

public class RepositoryInfoTest {

  @Test
  public void shouldReturnPath() {
    String path = "/path/someRepo.git";
    RepositoryInfo repoInfo = new RepositoryInfo(path, null, null, null, true);
    assertThat(repoInfo.getPath()).isEqualTo(path);
  }

  @Test
  public void shouldReturnQueuedAt() {
    Timestamp time = new Timestamp(System.currentTimeMillis());
    RepositoryInfo repoInfo = new RepositoryInfo(null, time, null, null, true);
    assertThat(repoInfo.getQueuedAt()).isEqualTo(time);
  }

  @Test
  public void shouldReturnExecutor() {
    String executor = "someHost-1";
    RepositoryInfo repoInfo = new RepositoryInfo(null, null, executor, null, true);
    assertThat(repoInfo.getExecutor()).isEqualTo(executor);
  }

  @Test
  public void shouldReturnQueuedFrom() {
    String hostname = "someHost-2";
    RepositoryInfo repoInfo = new RepositoryInfo(null, null, null, hostname, true);
    assertThat(repoInfo.getQueuedFrom()).isEqualTo(hostname);
  }
}
