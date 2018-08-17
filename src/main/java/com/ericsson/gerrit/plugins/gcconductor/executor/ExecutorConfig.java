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

import static java.time.ZoneId.systemDefault;

import com.ericsson.gerrit.plugins.gcconductor.CommonConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class ExecutorConfig extends CommonConfig {
  private static final Logger log = LoggerFactory.getLogger(ExecutorConfig.class);

  static final String CORE_SECTION = "core";
  static final String DB_SECTION = "db";
  static final String EVALUATION_SECTION = "evaluation";

  static final String DELAY_KEY = "delay";
  static final String EXECUTOR_KEY = "executors";
  static final String PICK_OWN_HOST_KEY = "pickOwnHostOnly";
  static final String REPOS_PATH_KEY = "repositoriesPath";
  static final String INTERVAL_KEY = "interval";
  static final String START_TIME_KEY = "startTime";

  static final String EMPTY = "";
  static final int DEFAULT_EXECUTORS = 2;
  static final int DEFAULT_DELAY = 0;
  static final String DEFAULT_REPOS_PATH = "/opt/gerrit/repos";
  static final long DEFAULT_INTERVAL = -1;
  static final long DEFAULT_INITIAL_DELAY = -1;

  private final int delay;
  private final int executors;
  private final boolean pickOwnHostOnly;
  private final String repositoriesPath;
  private final long interval;
  private final long initialDelay;

  @Inject
  ExecutorConfig(Config config) {
    super(
        getString(config, DB_SECTION, null, DB_URL_KEY, DEFAULT_DB_URL),
        getString(config, DB_SECTION, null, DB_NAME_KEY, DEFAULT_DB_NAME),
        Strings.nullToEmpty(getString(config, DB_SECTION, null, DB_URL_OPTIONS_KEY, EMPTY)),
        getString(config, DB_SECTION, null, DB_USERNAME_KEY, DEFAULT_DB_USERNAME),
        getString(config, DB_SECTION, null, DB_PASS_KEY, DEFAULT_DB_PASSWORD),
        config.getInt(EVALUATION_SECTION, PACKED_KEY, PACKED_DEFAULT),
        config.getInt(EVALUATION_SECTION, LOOSE_KEY, LOOSE_DEFAULT));
    delay = config.getInt(CORE_SECTION, DELAY_KEY, DEFAULT_DELAY);
    executors = config.getInt(CORE_SECTION, EXECUTOR_KEY, DEFAULT_EXECUTORS);
    pickOwnHostOnly = config.getBoolean(CORE_SECTION, PICK_OWN_HOST_KEY, true);
    repositoriesPath =
        getString(config, EVALUATION_SECTION, null, REPOS_PATH_KEY, DEFAULT_REPOS_PATH);
    interval = interval(config, EVALUATION_SECTION, INTERVAL_KEY);
    initialDelay =
        initialDelay(
            config.getString(EVALUATION_SECTION, null, START_TIME_KEY),
            ZonedDateTime.now(systemDefault()),
            interval);
  }

  int getExecutors() {
    return executors;
  }

  boolean isPickOwnHostOnly() {
    return pickOwnHostOnly;
  }

  int getDelay() {
    return delay;
  }

  String getRepositoriesPath() {
    return repositoriesPath;
  }

  long getInitialDelay() {
    return initialDelay;
  }

  long getInterval() {
    return interval;
  }

  private long interval(Config rc, String section, String key) {
    try {
      return ConfigUtil.getTimeUnit(rc, section, key, DEFAULT_INTERVAL, TimeUnit.MILLISECONDS);
    } catch (IllegalArgumentException e) {
      log.debug("Invalid {}.{} setting. Periodic evaluation disabled", section, key, e);
      return DEFAULT_INTERVAL;
    }
  }

  @VisibleForTesting
  long initialDelay(String start, ZonedDateTime now, long interval) {
    if (start == null) {
      return DEFAULT_INITIAL_DELAY;
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[E ]HH:mm").withLocale(Locale.US);
      LocalTime firstStartTime = LocalTime.parse(start, formatter);
      ZonedDateTime startTime = now.with(firstStartTime);
      Optional<DayOfWeek> dayOfWeek = getDayOfWeek(start, formatter);
      if (dayOfWeek.isPresent()) {
        startTime = startTime.with(dayOfWeek.get());
      }
      startTime = startTime.truncatedTo(ChronoUnit.MINUTES);
      long firstDelay = Duration.between(now, startTime).toMillis() % interval;
      if (firstDelay <= 0) {
        firstDelay += interval;
      }
      return firstDelay;
    } catch (DateTimeParseException e) {
      log.debug(
          "Invalid value {} for {} setting. Periodic evaluation disabled",
          start,
          START_TIME_KEY,
          e);
      return DEFAULT_INITIAL_DELAY;
    }
  }

  private Optional<DayOfWeek> getDayOfWeek(String start, DateTimeFormatter formatter) {
    try {
      return Optional.of(formatter.parse(start, DayOfWeek::from));
    } catch (DateTimeParseException ignored) {
      // Day of week is an optional parameter.
      return Optional.empty();
    }
  }
}
