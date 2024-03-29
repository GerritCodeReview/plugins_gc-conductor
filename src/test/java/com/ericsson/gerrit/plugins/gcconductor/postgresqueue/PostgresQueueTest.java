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

package com.ericsson.gerrit.plugins.gcconductor.postgresqueue;

import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.TestUtil.configMockFor;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.TestUtil.newContainer;
import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.testing.GerritJUnit.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresQueueTest {

  private BasicDataSource dataSource;
  private PostgresQueue queue;

  private static PostgreSQLContainer<?> container;

  @BeforeClass
  public static void startPostgres() {
    container = newContainer();
    container.start();
  }

  @Before
  public void setUp() throws Exception {
    dataSource = new PostgresModule(null).provideGcDatabaseAccess(configMockFor(container));
    queue = new PostgresQueue(dataSource);
    emptyQueue();
  }

  @After
  public void tearDown() throws Exception {
    if (dataSource != null) {
      dataSource.close();
    }
  }

  @Test
  public void shouldCreateSchemaOnInit() throws Exception {
    assertThat(queue.list()).isEmpty();
  }

  @Test
  public void shouldThrowExceptionIfFailsToCreateSchemaOnInit() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    when(dataSouceMock.getConnection()).thenThrow(new SQLException("some message"));
    assertThrows(SQLException.class, () -> queue = new PostgresQueue(dataSouceMock));
  }

  @Test
  public void testAddContainsAndRemove() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";

    assertThat(queue.list()).isEmpty();
    assertThat(queue.contains(repoPath)).isFalse();

    queue.add(repoPath, hostname, true);
    assertThat(queue.list().size()).isEqualTo(1);
    assertThat(queue.contains(repoPath)).isTrue();

    queue.add(repoPath, hostname, true);
    assertThat(queue.list().size()).isEqualTo(1);
    assertThat(queue.contains(repoPath)).isTrue();

    String repoPath2 = "/some/path/to/some/repository2";
    String hostname2 = "someHostname2";

    queue.add(repoPath2, hostname2, true);
    assertThat(queue.list().size()).isEqualTo(2);
    assertThat(queue.contains(repoPath)).isTrue();
    assertThat(queue.contains(repoPath2)).isTrue();

    queue.remove(repoPath2);
    assertThat(queue.list().size()).isEqualTo(1);
    assertThat(queue.contains(repoPath)).isTrue();
    assertThat(queue.contains(repoPath2)).isFalse();

    queue.remove(repoPath);
    assertThat(queue.list().size()).isEqualTo(0);
    assertThat(queue.contains(repoPath)).isFalse();

    queue.remove(repoPath);
    assertThat(queue.list().size()).isEqualTo(0);
    assertThat(queue.contains(repoPath)).isFalse();
  }

  @Test
  public void testAddThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.add("repo", "hostname", true));
  }

  @Test
  public void testAddThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.add("repo", "hostname", true));
  }

  @Test
  public void testAddThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.add("repo", "hostname", true));
  }

  @Test
  public void testContainsThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.contains("repo"));
  }

  @Test
  public void testContainsThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.contains("repo"));
  }

  @Test
  public void testContainsThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.contains("repo"));
  }

  @Test
  public void testContainsThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    assertThrows(GcQueueException.class, () -> queue.contains("repo"));
  }

  @Test
  public void testRemoveThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.remove("repo"));
  }

  @Test
  public void testRemoveThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.remove("repo"));
  }

  @Test
  public void testRemoveThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.remove("repo"));
  }

  @Test
  public void testList() throws Exception {
    String repoPath = "/some/path/to/some/repository.git";
    String repoPath2 = "/some/path/to/some/repository2.git";
    String hostname = "hostname";
    String executor = "hostname-1";

    assertThat(queue.list()).isEmpty();
    Timestamp before = new Timestamp(System.currentTimeMillis());
    queue.add(repoPath, hostname, true);
    queue.add(repoPath2, hostname, true);
    queue.pick(executor, 0, Optional.empty());

    assertThat(queue.list().size()).isEqualTo(2);

    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);
    assertThat(queue.list().get(0).getExecutor()).isEqualTo(executor);
    assertThat(queue.list().get(0).getQueuedAt()).isAtLeast(before);
    assertTimestampDiff(queue.list().get(0).getQueuedAt());
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(hostname);

    assertThat(queue.list().get(1).getPath()).isEqualTo(repoPath2);
    assertThat(queue.list().get(1).getExecutor()).isNull();
    assertThat(queue.list().get(1).getQueuedAt()).isAtLeast(queue.list().get(0).getQueuedAt());
    assertTimestampDiff(queue.list().get(1).getQueuedAt());
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(hostname);
  }

  private void assertTimestampDiff(Timestamp actual) {
    long timestampDiff = Math.abs(actual.getTime() - System.currentTimeMillis());
    assertThat(timestampDiff).isAtMost(TimeUnit.SECONDS.toMillis(1));
  }

  @Test
  public void testListThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.list());
  }

  @Test
  public void testListThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.list());
  }

  @Test
  public void testListThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.list());
  }

  @Test
  public void testListThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    assertThrows(GcQueueException.class, () -> queue.list());
  }

  @Test
  public void testPick() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";
    String executor2 = "someExecutor2";

    // queue is empty nothing to pick
    assertThat(queue.list()).isEmpty();
    assertThat(queue.pick(executor, 0, Optional.empty())).isNull();

    // queue contains 1 repository, should pick that one
    queue.add(repoPath, hostname, true);
    RepositoryInfo picked = queue.pick(executor, 0, Optional.empty());
    assertThat(picked).isNotNull();
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);

    // queue contains 1 already picked repository, should pick same one
    picked = queue.pick(executor, 0, Optional.empty());
    assertThat(picked).isNotNull();
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);

    // queue contains 1 already picked repository, nothing to pick for other
    // executors
    assertThat(queue.pick(executor2, 0, Optional.empty())).isNull();
  }

  @Test
  public void testPickRepositoriesInOrder() throws Exception {
    String repositoryFormat = "my/path%s.git";
    for (int i = 0; i < 100; i++) {
      queue.add(String.format(repositoryFormat, i), "someHostname", true);
    }
    for (int i = 0; i < 100; i++) {
      String pickedRepo = queue.pick("someExecutor", 0, Optional.empty()).getPath();
      assertThat(pickedRepo).isEqualTo(String.format(repositoryFormat, i));
      queue.remove(pickedRepo);
    }
  }

  @Test
  public void testPickInQueueForLongerThan() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";

    // pick repository older than 10 seconds, nothing to pick
    queue.add(repoPath, hostname, true);
    assertThat(queue.pick(executor, 10, Optional.empty())).isNull();
    assertThat(queue.list().get(0).getExecutor()).isNull();

    // make 2 seconds elapse and pick repository older than 1 second, should pick one
    TimeUnit.SECONDS.sleep((2));
    RepositoryInfo picked = queue.pick(executor, 1, Optional.empty());
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);
  }

  @Test
  public void testPickQueuedFrom() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "hostname";
    String otherHostname = "otherHostname";
    String executor = "hostname-1";

    // pick repository queued from otherHostname, nothing to pick
    queue.add(repoPath, hostname, true);
    assertThat(queue.pick(executor, 0, Optional.of(otherHostname))).isNull();
    assertThat(queue.list().get(0).getExecutor()).isNull();

    // pick repository queued from hostname, should pick one
    RepositoryInfo picked = queue.pick(executor, 0, Optional.of(hostname));
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);
  }

  @Test
  public void testPickThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.pick("executor", 0, Optional.empty()));
  }

  @Test
  public void testPickThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.pick("executor", 0, Optional.empty()));
  }

  @Test
  public void testPickThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.pick("executor", 0, Optional.empty()));
  }

  @Test
  public void testPickThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    assertThrows(GcQueueException.class, () -> queue.pick("executor", 0, Optional.empty()));
  }

  @Test
  public void testUnpick() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";

    // queue contains 1 repository, should pick that one
    queue.add(repoPath, hostname, true);
    RepositoryInfo picked = queue.pick(executor, 0, Optional.empty());
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);

    queue.unpick(repoPath);
    // unpick repo so should pick that one again
    queue.unpick(repoPath);
    picked = queue.pick(executor, 0, Optional.empty());
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);
  }

  @Test
  public void testUnpickThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.unpick("/some/path/to/some/repository.git"));
  }

  @Test
  public void testUnpickFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.unpick("/some/path/to/some/repository.git"));
  }

  @Test
  public void testUnpickThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.unpick("/some/path/to/some/repository.git"));
  }

  @Test
  public void testResetQueuedFrom() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String repoPath2 = "/some/path/to/some/repository2";
    String hostname = "hostname";
    String otherHostname = "otherHostname";

    queue.add(repoPath, hostname, true);
    queue.add(repoPath2, hostname, true);
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(hostname);
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(hostname);

    queue.resetQueuedFrom(otherHostname);
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(otherHostname);
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(otherHostname);
  }

  @Test
  public void testResetQueuedFromThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.resetQueuedFrom("someHostname"));
  }

  @Test
  public void testResetQueuedFromFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.resetQueuedFrom("someHostname"));
  }

  @Test
  public void testResetQueuedFromThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.resetQueuedFrom("someHostname"));
  }

  @Test
  public void testBumpToFirst() throws Exception {
    String repoPath = "/some/path/to/some/repository";
    String repoPath2 = "/some/path/to/some/repository2";
    String repoPath3 = "/some/path/to/some/repository3";
    String hostname = "hostname";

    // Queue contains 1 repository, bumping should have no effect
    queue.add(repoPath, hostname, true);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);
    queue.bumpToFirst(repoPath);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);

    // Queue has 3 repositories, should be able to change their order
    queue.add(repoPath2, hostname, true);
    queue.add(repoPath3, hostname, true);
    assertThat(queue.list().get(1).getPath()).isEqualTo(repoPath2);
    assertThat(queue.list().get(2).getPath()).isEqualTo(repoPath3);

    // repoPath3 should be first, all other repositories should be shifted down
    queue.bumpToFirst(repoPath3);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath3);
    assertThat(queue.list().get(1).getPath()).isEqualTo(repoPath);
    assertThat(queue.list().get(2).getPath()).isEqualTo(repoPath2);

    // Bumping a repository that is already first priority should have no effect
    queue.bumpToFirst(repoPath3);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath3);
    assertThat(queue.list().get(1).getPath()).isEqualTo(repoPath);
    assertThat(queue.list().get(2).getPath()).isEqualTo(repoPath2);
  }

  @Test
  public void testBumpToFirstThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    assertThrows(GcQueueException.class, () -> queue.bumpToFirst("someHostname"));
  }

  @Test
  public void testBumpToFirstFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    assertThrows(GcQueueException.class, () -> queue.bumpToFirst("someHostname"));
  }

  @Test
  public void testBumpToFirstThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    assertThrows(GcQueueException.class, () -> queue.bumpToFirst("someHostname"));
  }

  private BasicDataSource createDataSourceThatFailsWhenGettingConnection() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock).thenThrow(new SQLException());
    when(connectionMock.createStatement()).thenReturn(statementMock);

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenCreatingStatement() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock).thenThrow(new SQLException());

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenExecutingQuery() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString())).thenReturn(true).thenThrow(new SQLException());
    when(statementMock.executeQuery(anyString())).thenThrow(new SQLException());

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenIteratingResults() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);
    ResultSet resultSetMock = mock(ResultSet.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.executeQuery(anyString())).thenReturn(resultSetMock);
    when(resultSetMock.next()).thenThrow(new SQLException());

    return dataSouceMock;
  }

  private void emptyQueue() throws Exception {
    queue.list().stream().map(RepositoryInfo::getPath).forEach(this::doEmptyQueue);
    assertThat(queue.list()).isEmpty();
  }

  private void doEmptyQueue(String repository) {
    try {
      queue.remove(repository);
    } catch (GcQueueException e) {
      throw new RuntimeException(e);
    }
  }
}
