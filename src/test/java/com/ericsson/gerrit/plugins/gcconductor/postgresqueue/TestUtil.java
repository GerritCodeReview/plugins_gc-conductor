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

import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.DRIVER;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.INITIAL_DATABASE;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.dropDatabase;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.executeStatement;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;

@Ignore
public class TestUtil {

  private static final String DATABASE_SERVER_URL = "jdbc:postgresql://localhost:5432/";
  private static final String DEFAULT_USER_AND_PASSWORD = "gc";

  static EvaluatorConfig configMockFor(String databaseName) {
    EvaluatorConfig configMock = mock(EvaluatorConfig.class);
    when(configMock.getDatabaseUrl()).thenReturn(DATABASE_SERVER_URL);
    when(configMock.getDatabaseUrlOptions()).thenReturn("");
    when(configMock.getDatabaseName()).thenReturn(databaseName);
    when(configMock.getUsername()).thenReturn(DEFAULT_USER_AND_PASSWORD);
    when(configMock.getPassword()).thenReturn(DEFAULT_USER_AND_PASSWORD);
    return configMock;
  }

  static void deleteDatabase(String databaseName) throws SQLException {
    BasicDataSource ds = new BasicDataSource();
    try {
      ds.setDriverClassName(DRIVER);
      ds.setUrl(DATABASE_SERVER_URL + INITIAL_DATABASE);
      ds.setUsername(DEFAULT_USER_AND_PASSWORD);
      ds.setPassword(DEFAULT_USER_AND_PASSWORD);
      executeStatement(ds, dropDatabase(databaseName));
    } finally {
      ds.close();
    }
  }
}
