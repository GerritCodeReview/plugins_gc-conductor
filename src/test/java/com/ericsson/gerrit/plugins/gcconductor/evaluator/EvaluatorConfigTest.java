// Copyright (C) 2016 Ericsson
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

import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DB_NAME_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DB_PASS_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DB_URL_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DB_URL_OPTIONS_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DB_USERNAME_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DEFAULT_DB_NAME;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DEFAULT_DB_PASSWORD;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DEFAULT_DB_URL;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.DEFAULT_DB_USERNAME;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.LOOSE_DEFAULT;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.LOOSE_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.PACKED_DEFAULT;
import static com.ericsson.gerrit.plugins.gcconductor.CommonConfig.PACKED_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig.EXPIRE_TIME_RECHECK_DEFAULT;
import static com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig.EXPIRE_TIME_RECHECK_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig.THREAD_POOL_DEFAULT;
import static com.ericsson.gerrit.plugins.gcconductor.evaluator.EvaluatorConfig.THREAD_POOL_KEY;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.gerrit.server.config.ConfigUtil;
import com.google.gerrit.server.config.PluginConfig;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EvaluatorConfigTest {

  private static final int PACKED_NOT_DEFAULT = 41;
  private static final int LOOSE_NOT_DEFAULT = 401;
  private static final String URL_NOT_DEFAULT = "jdbc:postgresql:test/";
  private static final String DATABASE_NAME_NOT_DEFAULT = "jdbc:postgresql:test/";
  private static final String URL_OPTION_NOT_DEFAULT = "?someOption=false";
  private static final String USER_NOT_DEFAULT = "user";
  private static final String PASS_NOT_DEFAULT = "pass";
  private static final int THREAD_POOL_NOT_DEFAULT = 5;
  private static final String EXPIRE_TIME_RECHECK_NOT_DEFAULT = "10s";
  private static final boolean USING_DEFAULT_VALUES = true;
  private static final boolean USING_CUSTOM_VALUES = false;

  @Mock private PluginConfig pluginConfigMock;
  private EvaluatorConfig configuration;

  @Test
  public void testValuesNotPresentInGerritConfig() {
    buildMocks(USING_DEFAULT_VALUES);
    assertThat(configuration.getPackedThreshold()).isEqualTo(PACKED_DEFAULT);
    assertThat(configuration.getLooseThreshold()).isEqualTo(LOOSE_DEFAULT);
    assertThat(configuration.getDatabaseUrl()).isEqualTo(DEFAULT_DB_URL);
    assertThat(configuration.getDatabaseName()).isEqualTo(DEFAULT_DB_NAME);
    assertThat(configuration.getDatabaseUrlOptions()).isEmpty();
    assertThat(configuration.getUsername()).isEqualTo(DEFAULT_DB_USERNAME);
    assertThat(configuration.getPassword()).isEqualTo(DEFAULT_DB_PASSWORD);
    assertThat(configuration.getThreadPoolSize()).isEqualTo(THREAD_POOL_DEFAULT);
    assertThat(configuration.getExpireTimeRecheck())
        .isEqualTo(convertTimeUnitStringToMilliseconds(EXPIRE_TIME_RECHECK_DEFAULT));
  }

  @Test
  public void testValuesPresentInGerritConfig() {
    buildMocks(USING_CUSTOM_VALUES);
    assertThat(configuration.getPackedThreshold()).isEqualTo(PACKED_NOT_DEFAULT);
    assertThat(configuration.getLooseThreshold()).isEqualTo(LOOSE_NOT_DEFAULT);
    assertThat(configuration.getDatabaseUrl()).isEqualTo(URL_NOT_DEFAULT);
    assertThat(configuration.getDatabaseName()).isEqualTo(DATABASE_NAME_NOT_DEFAULT);
    assertThat(configuration.getDatabaseUrlOptions()).isEqualTo(URL_OPTION_NOT_DEFAULT);
    assertThat(configuration.getUsername()).isEqualTo(USER_NOT_DEFAULT);
    assertThat(configuration.getPassword()).isEqualTo(PASS_NOT_DEFAULT);
    assertThat(configuration.getThreadPoolSize()).isEqualTo(THREAD_POOL_NOT_DEFAULT);
    assertThat(configuration.getExpireTimeRecheck())
        .isEqualTo(convertTimeUnitStringToMilliseconds(EXPIRE_TIME_RECHECK_NOT_DEFAULT));
  }

  @Test
  public void testDatabaseUrlAlwaysEndWithSlash() {
    when(pluginConfigMock.getString(EXPIRE_TIME_RECHECK_KEY, EXPIRE_TIME_RECHECK_DEFAULT))
        .thenReturn(EXPIRE_TIME_RECHECK_DEFAULT);
    when(pluginConfigMock.getString(DB_URL_KEY, DEFAULT_DB_URL)).thenReturn("someUrl");

    configuration = new EvaluatorConfig(pluginConfigMock);
    assertThat(configuration.getDatabaseUrl()).isEqualTo("someUrl/");
  }

  private void buildMocks(boolean useDefaults) {
    when(pluginConfigMock.getInt(PACKED_KEY, PACKED_DEFAULT))
        .thenReturn(useDefaults ? PACKED_DEFAULT : PACKED_NOT_DEFAULT);
    when(pluginConfigMock.getInt(LOOSE_KEY, LOOSE_DEFAULT))
        .thenReturn(useDefaults ? LOOSE_DEFAULT : LOOSE_NOT_DEFAULT);
    when(pluginConfigMock.getString(DB_URL_KEY, DEFAULT_DB_URL))
        .thenReturn(useDefaults ? DEFAULT_DB_URL : URL_NOT_DEFAULT);
    when(pluginConfigMock.getString(DB_NAME_KEY, DEFAULT_DB_NAME))
        .thenReturn(useDefaults ? DEFAULT_DB_NAME : DATABASE_NAME_NOT_DEFAULT);
    when(pluginConfigMock.getString(DB_URL_OPTIONS_KEY))
        .thenReturn(useDefaults ? null : URL_OPTION_NOT_DEFAULT);
    when(pluginConfigMock.getString(DB_USERNAME_KEY, DEFAULT_DB_USERNAME))
        .thenReturn(useDefaults ? DEFAULT_DB_USERNAME : USER_NOT_DEFAULT);
    when(pluginConfigMock.getString(DB_PASS_KEY, DEFAULT_DB_PASSWORD))
        .thenReturn(useDefaults ? DEFAULT_DB_PASSWORD : PASS_NOT_DEFAULT);
    when(pluginConfigMock.getInt(THREAD_POOL_KEY, THREAD_POOL_DEFAULT))
        .thenReturn(useDefaults ? THREAD_POOL_DEFAULT : THREAD_POOL_NOT_DEFAULT);
    when(pluginConfigMock.getString(EXPIRE_TIME_RECHECK_KEY, EXPIRE_TIME_RECHECK_DEFAULT))
        .thenReturn(useDefaults ? EXPIRE_TIME_RECHECK_DEFAULT : EXPIRE_TIME_RECHECK_NOT_DEFAULT);

    configuration = new EvaluatorConfig(pluginConfigMock);
  }

  private long convertTimeUnitStringToMilliseconds(String string) {
    return ConfigUtil.getTimeUnit(string, -1, TimeUnit.MILLISECONDS);
  }
}
