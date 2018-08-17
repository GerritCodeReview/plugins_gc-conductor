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

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lib.Config;

class ConfigUtil {

  private ConfigUtil() {}

  /**
   * Parse a numerical time unit, such as "1 minute", from the configuration.
   *
   * @param config the configuration file to read.
   * @param section section the key is in.
   * @param setting name of the setting to read.
   * @param defaultValue default value to return if no value was set in the configuration file.
   * @param wantUnit the units of {@code defaultValue} and the return value, as well as the units to
   *     assume if the value does not contain an indication of the units.
   * @return the setting, or {@code defaultValue} if not set, expressed in {@code units}.
   */
  public static long getTimeUnit(
      Config config, String section, String setting, long defaultValue, final TimeUnit wantUnit) {
    String value = config.getString(section, null, setting);
    if (value == null) {
      return defaultValue;
    }

    String s = value.trim();
    if (s.length() == 0) {
      return defaultValue;
    }

    if (s.startsWith("-") /* negative */) {
      throw notTimeUnit(section, setting, value);
    }

    try {
      return getTimeUnit(s, defaultValue, wantUnit);
    } catch (IllegalArgumentException notTime) {
      throw notTimeUnit(section, setting, value);
    }
  }

  /**
   * Parse a numerical time unit, such as "1 minute", from a string.
   *
   * @param valueString the string to parse.
   * @param defaultValue default value to return if no value was set in the configuration file.
   * @param wantUnit the units of {@code defaultValue} and the return value, as well as the units to
   *     assume if the value does not contain an indication of the units.
   * @return the setting, or {@code defaultValue} if not set, expressed in {@code units}.
   */
  public static long getTimeUnit(String valueString, long defaultValue, TimeUnit wantUnit) {
    Matcher m = Pattern.compile("^(0|[1-9][0-9]*)\\s*(.*)$").matcher(valueString);
    if (!m.matches()) {
      return defaultValue;
    }

    String digits = m.group(1);
    String unitName = m.group(2).trim();

    TimeUnit inputUnit;
    int inputMul;

    if (match(unitName, "h", "hour", "hours")) {
      inputUnit = TimeUnit.HOURS;
      inputMul = 1;
    } else if ("".equals(unitName) || match(unitName, "d", "day", "days")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 1;
    } else if (match(unitName, "w", "week", "weeks")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 7;
    } else if (match(unitName, "mon", "month", "months")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 30;
    } else {
      throw notTimeUnit(valueString);
    }

    try {
      return wantUnit.convert(Long.parseLong(digits) * inputMul, inputUnit);
    } catch (NumberFormatException nfe) {
      throw notTimeUnit(valueString);
    }
  }

  private static boolean match(String a, String... cases) {
    for (String b : cases) {
      if (b.equalsIgnoreCase(a)) {
        return true;
      }
    }
    return false;
  }

  private static IllegalArgumentException notTimeUnit(
      String section, String setting, String valueString) {
    return new IllegalArgumentException(
        "Invalid time unit value: " + section + "." + setting + " = " + valueString);
  }

  private static IllegalArgumentException notTimeUnit(String val) {
    return new IllegalArgumentException("Invalid time unit value: " + val);
  }
}
