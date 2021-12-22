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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig;
import org.junit.Ignore;
import org.testcontainers.containers.PostgreSQLContainer;

@Ignore
public class TestUtil {
  private static final String LABEL = PostgreSQLContainer.IMAGE + ":" + "9.6.12-alpine";
  //private static final String LABEL = PostgreSQLContainer.IMAGE + ":" + "latest";

  static PostgreSQLContainer<?> newContainer() {
    return new PostgreSQLContainer<>(LABEL);
  }

  static EvaluatorConfig configMockFor(PostgreSQLContainer<?> container) {
    EvaluatorConfig configMock = mock(EvaluatorConfig.class);
    when(configMock.getDatabaseUrl()).thenReturn(urlWithoutDatabase(container));
    when(configMock.getDatabaseUrlOptions()).thenReturn("");
    when(configMock.getDatabaseName()).thenReturn(container.getDatabaseName());
    when(configMock.getUsername()).thenReturn(container.getUsername());
    when(configMock.getPassword()).thenReturn(container.getPassword());
    return configMock;
  }

  static EvaluatorConfig invalidConfigMockFor(PostgreSQLContainer<?> container) {
    EvaluatorConfig configMock = configMockFor(container);
    when(configMock.getDatabaseName()).thenReturn(container.getDatabaseName() + "not");
    return configMock;
  }

  private static String urlWithoutDatabase(PostgreSQLContainer<?> container) {
    String urlWithDatabase = container.getJdbcUrl();
    return urlWithDatabase.substring(0, urlWithDatabase.lastIndexOf('/') + 1);
  }
}
