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
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.TestUtil.deleteDatabase;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig;
import com.ericsson.gerrit.plugins.gcconductor.postgresqueue.PostgresModule.DatabaseAccessCleanUp;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PostgresModuleTest {

  private static final String TEST_DATABASE_NAME = "gc_test_module";
  private EvaluatorConfig configMock;
  private PostgresModule module;
  private BasicDataSource dataSource;

  @Before
  public void setUp() throws SQLException {
    configMock = configMockFor(TEST_DATABASE_NAME);
    module = new PostgresModule(null);
    dataSource = module.provideGcDatabaseAccess(configMock);
  }

  @After
  public void tearDown() throws SQLException {
    if (dataSource != null) {
      dataSource.close();
    }
    deleteDatabase(TEST_DATABASE_NAME);
  }

  @Test
  public void shouldCreateGcDatabase() {
    assertThat(dataSource).isNotNull();
    assertThat(dataSource.isClosed()).isFalse();
  }

  @Test
  public void shouldNotComplainsIfGcDatabaseAlreadyExists() throws SQLException {
    dataSource.close();
    dataSource = module.provideGcDatabaseAccess(configMock);
    assertThat(dataSource).isNotNull();
    assertThat(dataSource.isClosed()).isFalse();
  }

  @Test(expected = SQLException.class)
  public void shouldFailIfDatabaseNameIsInvalid() throws SQLException {
    module.provideGcDatabaseAccess(configMockFor("invalid!@#$@$%^-name"));
  }

  @Test
  public void shouldCloseDatabaseAccessOnStop() {
    DatabaseAccessCleanUp dbCleanUp = new DatabaseAccessCleanUp(dataSource);
    assertThat(dataSource).isNotNull();
    assertThat(dataSource.isClosed()).isFalse();
    dbCleanUp.onShutdown();
    assertThat(dataSource.isClosed()).isTrue();
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionIfFailsToCloseDatabaseAccess() throws SQLException {
    BasicDataSource dataSouceMock = mock(BasicDataSource.class);
    doThrow(new SQLException("somme error")).when(dataSouceMock).close();
    DatabaseAccessCleanUp dbCleanUp = new DatabaseAccessCleanUp(dataSouceMock);
    dbCleanUp.onShutdown();
  }
}
