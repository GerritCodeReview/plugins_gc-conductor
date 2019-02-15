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
import static com.google.common.truth.Truth.assertThat;
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
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresQueueTest {

  private BasicDataSource dataSource;
  private PostgresQueue queue;

  private static PostgreSQLContainer<?> container;

  @BeforeClass
  public static void startPostgres() {
    container = new PostgreSQLContainer<>();
    container.start();
  }

  @Before
  public void setUp() throws SQLException {
    dataSource = new PostgresModule(null).provideGcDatabaseAccess(configMockFor(container));
    queue = new PostgresQueue(dataSource);
  }

  @After
  public void tearDown() throws SQLException {
    if (dataSource != null) {
      dataSource.close();
    }
  }

  @Test
  public void shouldCreateSchemaOnInit() throws GcQueueException {
    assertThat(queue.list()).isEmpty();
  }

  @Test(expected = SQLException.class)
  public void shouldThrowExceptionIfFailsToCreateSchemaOnInit() throws Exception {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    when(dataSouceMock.getConnection()).thenThrow(new SQLException("some message"));
    queue = new PostgresQueue(dataSouceMock);
  }

  @Test
  public void testAddContainsAndRemove() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";

    assertThat(queue.list()).isEmpty();
    assertThat(queue.contains(repoPath)).isFalse();

    queue.add(repoPath, hostname);
    assertThat(queue.list().size()).isEqualTo(1);
    assertThat(queue.contains(repoPath)).isTrue();

    queue.add(repoPath, hostname);
    assertThat(queue.list().size()).isEqualTo(1);
    assertThat(queue.contains(repoPath)).isTrue();

    String repoPath2 = "/some/path/to/some/repository2";
    String hostname2 = "someHostname2";

    queue.add(repoPath2, hostname2);
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

  @Test(expected = GcQueueException.class)
  public void testAddThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.add("repo", "hostname");
  }

  @Test(expected = GcQueueException.class)
  public void testAddThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.add("repo", "hostname");
  }

  @Test(expected = GcQueueException.class)
  public void testAddThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.add("repo", "hostname");
  }

  @Test(expected = GcQueueException.class)
  public void testContainsThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.contains("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testContainsThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.contains("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testContainsThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.contains("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testContainsThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    queue.contains("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testRemoveThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.remove("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testRemoveThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.remove("repo");
  }

  @Test(expected = GcQueueException.class)
  public void testRemoveThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.remove("repo");
  }

  @Ignore
  @Test
  public void testList() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository.git";
    String repoPath2 = "/some/path/to/some/repository2.git";
    String hostname = "hostname";
    String executor = "hostname-1";

    assertThat(queue.list()).isEmpty();
    Timestamp before = new Timestamp(System.currentTimeMillis());
    queue.add(repoPath, hostname);
    queue.add(repoPath2, hostname);
    queue.pick(executor, 0, Optional.empty());

    assertThat(queue.list().size()).isEqualTo(2);

    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);
    assertThat(queue.list().get(0).getExecutor()).isEqualTo(executor);
    assertThat(queue.list().get(0).getQueuedAt()).isAtLeast(before);
    assertThat(queue.list().get(0).getQueuedAt())
        .isAtMost(new Timestamp(System.currentTimeMillis()));
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(hostname);

    assertThat(queue.list().get(1).getPath()).isEqualTo(repoPath2);
    assertThat(queue.list().get(1).getExecutor()).isNull();
    assertThat(queue.list().get(1).getQueuedAt()).isAtLeast(queue.list().get(0).getQueuedAt());
    assertThat(queue.list().get(1).getQueuedAt())
        .isAtMost(new Timestamp(System.currentTimeMillis()));
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(hostname);
  }

  @Test(expected = GcQueueException.class)
  public void testListThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.list();
  }

  @Test(expected = GcQueueException.class)
  public void testListThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.list();
  }

  @Test(expected = GcQueueException.class)
  public void testListThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.list();
  }

  @Test(expected = GcQueueException.class)
  public void testListThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    queue.list();
  }

  @Ignore
  @Test
  public void testPick() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";
    String executor2 = "someExecutor2";

    // queue is empty nothing to pick
    assertThat(queue.list()).isEmpty();
    assertThat(queue.pick(executor, 0, Optional.empty())).isNull();

    // queue contains 1 repository, should pick that one
    queue.add(repoPath, hostname);
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
  public void testPickRepositoriesInOrder() throws GcQueueException {
    String repositoryFormat = "my/path%s.git";
    for (int i = 0; i < 100; i++) {
      queue.add(String.format(repositoryFormat, i), "someHostname");
    }
    for (int i = 0; i < 100; i++) {
      String pickedRepo = queue.pick("someExecutor", 0, Optional.empty()).getPath();
      assertThat(pickedRepo).isEqualTo(String.format(repositoryFormat, i));
      queue.remove(pickedRepo);
    }
  }

  @Ignore
  @Test
  public void testPickInQueueForLongerThan() throws GcQueueException, InterruptedException {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";

    // pick repository older than 10 seconds, nothing to pick
    queue.add(repoPath, hostname);
    assertThat(queue.pick(executor, 10, Optional.empty())).isNull();
    assertThat(queue.list().get(0).getExecutor()).isNull();

    // make 2 seconds elapse and pick repository older than 1 second, should pick one
    TimeUnit.SECONDS.sleep((2));
    RepositoryInfo picked = queue.pick(executor, 1, Optional.empty());
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);
  }

  @Ignore
  @Test
  public void testPickQueuedFrom() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "hostname";
    String otherHostname = "otherHostname";
    String executor = "hostname-1";

    // pick repository queued from otherHostname, nothing to pick
    queue.add(repoPath, hostname);
    assertThat(queue.pick(executor, 0, Optional.of(otherHostname))).isNull();
    assertThat(queue.list().get(0).getExecutor()).isNull();

    // pick repository queued from hostname, should pick one
    RepositoryInfo picked = queue.pick(executor, 0, Optional.of(hostname));
    assertThat(picked.getPath()).isEqualTo(repoPath);
    assertThat(picked.getExecutor()).isEqualTo(executor);
  }

  @Test(expected = GcQueueException.class)
  public void testPickThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.pick("executor", 0, Optional.empty());
  }

  @Test(expected = GcQueueException.class)
  public void testPickThatFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.pick("executor", 0, Optional.empty());
  }

  @Test(expected = GcQueueException.class)
  public void testPickThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.pick("executor", 0, Optional.empty());
  }

  @Test(expected = GcQueueException.class)
  public void testPickThatFailsWhenIteratingResults() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenIteratingResults());
    queue.pick("executor", 0, Optional.empty());
  }

  @Ignore
  @Test
  public void testUnpick() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String hostname = "someHostname";
    String executor = "someExecutor";

    // queue contains 1 repository, should pick that one
    queue.add(repoPath, hostname);
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

  @Test(expected = GcQueueException.class)
  public void testUnpickThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.unpick("/some/path/to/some/repository.git");
  }

  @Test(expected = GcQueueException.class)
  public void testUnpickFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.unpick("/some/path/to/some/repository.git");
  }

  @Test(expected = GcQueueException.class)
  public void testUnpickThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.unpick("/some/path/to/some/repository.git");
  }

  @Ignore
  @Test
  public void testResetQueuedFrom() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String repoPath2 = "/some/path/to/some/repository2";
    String hostname = "hostname";
    String otherHostname = "otherHostname";

    queue.add(repoPath, hostname);
    queue.add(repoPath2, hostname);
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(hostname);
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(hostname);

    queue.resetQueuedFrom(otherHostname);
    assertThat(queue.list().get(0).getQueuedFrom()).isEqualTo(otherHostname);
    assertThat(queue.list().get(1).getQueuedFrom()).isEqualTo(otherHostname);
  }

  @Test(expected = GcQueueException.class)
  public void testResetQueuedFromThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.resetQueuedFrom("someHostname");
  }

  @Test(expected = GcQueueException.class)
  public void testResetQueuedFromFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.resetQueuedFrom("someHostname");
  }

  @Test(expected = GcQueueException.class)
  public void testResetQueuedFromThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.resetQueuedFrom("someHostname");
  }

  @Ignore
  @Test
  public void testBumpToFirst() throws GcQueueException {
    String repoPath = "/some/path/to/some/repository";
    String repoPath2 = "/some/path/to/some/repository2";
    String repoPath3 = "/some/path/to/some/repository3";
    String hostname = "hostname";

    // Queue contains 1 repository, bumping should have no effect
    queue.add(repoPath, hostname);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);
    queue.bumpToFirst(repoPath);
    assertThat(queue.list().get(0).getPath()).isEqualTo(repoPath);

    // Queue has 3 repositories, should be able to change their order
    queue.add(repoPath2, hostname);
    queue.add(repoPath3, hostname);
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

  @Test(expected = GcQueueException.class)
  public void testBumpToFirstThatFailsWhenGettingConnection() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenGettingConnection());
    queue.bumpToFirst("someHostname");
  }

  @Test(expected = GcQueueException.class)
  public void testBumpToFirstFailsWhenCreatingStatement() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenCreatingStatement());
    queue.bumpToFirst("someHostname");
  }

  @Test(expected = GcQueueException.class)
  public void testBumpToFirstThatFailsWhenExecutingQuery() throws Exception {
    queue = new PostgresQueue(createDataSourceThatFailsWhenExecutingQuery());
    queue.bumpToFirst("someHostname");
  }

  private BasicDataSource createDataSourceThatFailsWhenGettingConnection() throws SQLException {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock).thenThrow(new SQLException());
    when(connectionMock.createStatement()).thenReturn(statementMock);

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenCreatingStatement() throws SQLException {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock).thenThrow(new SQLException());

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenExecutingQuery() throws SQLException {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    Connection connectionMock = mock(Connection.class);
    Statement statementMock = mock(Statement.class);

    when(dataSouceMock.getConnection()).thenReturn(connectionMock);
    when(connectionMock.createStatement()).thenReturn(statementMock);
    when(statementMock.execute(anyString())).thenReturn(true).thenThrow(new SQLException());
    when(statementMock.executeQuery(anyString())).thenThrow(new SQLException());

    return dataSouceMock;
  }

  private BasicDataSource createDataSourceThatFailsWhenIteratingResults() throws SQLException {
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
}
