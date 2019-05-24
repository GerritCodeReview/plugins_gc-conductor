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

package com.ericsson.gerrit.plugins.gcconductor.executor;

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.testing.GerritJUnit.assertThrows;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigUtilTest {

  private static final int DEFAULT_VALUE = 1;

  @Mock private Config config;

  @Test
  public void testGetTimeShouldReturnValue() {
    when(config.getString("", null, "")).thenReturn("1 WEEK");
    assertThat(ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS)).isEqualTo(7);
  }

  @Test
  public void testGetTimeShouldReturnDefault() {
    when(config.getString("", null, "")).thenReturn(null);
    assertThat(ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS))
        .isEqualTo(DEFAULT_VALUE);

    when(config.getString("", null, "")).thenReturn("");
    assertThat(ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS))
        .isEqualTo(DEFAULT_VALUE);
  }

  @Test
  public void testGetTimeThrowsExceptionIfNegativeValue() {
    when(config.getString("", null, "")).thenReturn("-1");
    assertThrows(
        IllegalArgumentException.class,
        () -> ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS));

    when(config.getString("", null, "")).thenReturn("");
    assertThat(ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS))
        .isEqualTo(DEFAULT_VALUE);
  }

  @Test
  public void testGetTimeThrowsExceptionIfBadTimeUnit() {
    when(config.getString("", null, "")).thenReturn("1s");
    assertThrows(
        IllegalArgumentException.class,
        () -> ConfigUtil.getTimeUnit(config, "", "", DEFAULT_VALUE, DAYS));
  }

  @Test
  public void testTimeUnit() {
    assertEquals(ms(5, HOURS), parse("5h"));
    assertEquals(ms(1, HOURS), parse("1hour"));
    assertEquals(ms(48, HOURS), parse("48hours"));

    assertEquals(ms(5, HOURS), parse("5 h"));
    assertEquals(ms(1, HOURS), parse("1 hour"));
    assertEquals(ms(48, HOURS), parse("48 hours"));
    assertEquals(ms(48, HOURS), parse("48 \t \r hours"));

    assertEquals(ms(4, DAYS), parse("4 d"));
    assertEquals(ms(1, DAYS), parse("1day"));
    assertEquals(ms(14, DAYS), parse("14days"));

    assertEquals(ms(7, DAYS), parse("1 w"));
    assertEquals(ms(7, DAYS), parse("1week"));
    assertEquals(ms(14, DAYS), parse("2w"));
    assertEquals(ms(14, DAYS), parse("2weeks"));

    assertEquals(ms(30, DAYS), parse("1 mon"));
    assertEquals(ms(30, DAYS), parse("1month"));
    assertEquals(ms(60, DAYS), parse("2mon"));
    assertEquals(ms(60, DAYS), parse("2months"));

    assertEquals(ms(60, DAYS), parse("60"));
    assertEquals(ms(1, MILLISECONDS), parse(""));
  }

  @Test
  public void testUnsupportedTimeUnit() {
    assertThrows(IllegalArgumentException.class, () -> parse("1 min"));
  }

  private static long ms(int cnt, TimeUnit unit) {
    return MILLISECONDS.convert(cnt, unit);
  }

  private static long parse(String string) {
    return ConfigUtil.getTimeUnit(string, 1, MILLISECONDS);
  }
}
