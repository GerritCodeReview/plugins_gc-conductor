package com.ericsson.gerrit.plugins.gcconductor.executor;

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
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.CORE_SECTION;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DB_SECTION;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DEFAULT_DELAY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DEFAULT_EXECUTORS;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DEFAULT_INITIAL_DELAY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DEFAULT_INTERVAL;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DEFAULT_REPOS_PATH;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.DELAY_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.EMPTY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.EVALUATION_SECTION;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.EXECUTOR_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.INTERVAL_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.PICK_OWN_HOST_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.REPOS_PATH_KEY;
import static com.ericsson.gerrit.plugins.gcconductor.executor.ExecutorConfig.START_TIME_KEY;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.Config;
import org.junit.Before;
import org.junit.Test;

public class ExecutorConfigTest {
  private static final String CUSTOM_REPOS_PATH = "/other/path/to/repos";
  private static final String CUSTOM_DB_URL = "customDbUrl/";
  private static final String CUSTOM_DB_NAME = "customDbName";
  private static final String URL_OPTIONS = "urlOptions";
  private static final String CUSTOM_DB_USER = "customDbUser";
  private static final String CUSTOM_DB_PASS = "customDbPass";
  private static final int CUSTOM_DELAY = 5;
  private static final int CUSTOM_EXECUTORS = 10;
  private static final int CUSTOM_PACKED = 41;
  private static final int CUSTOM_LOOSE = 401;
  private static final int ONE_DAY_AS_MS = 86_400_000;

  // Friday June 13, 2014 10:00 UTC
  private static final ZonedDateTime NOW =
      LocalDateTime.of(2014, Month.JUNE, 13, 10, 0, 0).atOffset(ZoneOffset.UTC).toZonedDateTime();

  private Config config;
  private ExecutorConfig executorConfig;

  @Before
  public void setUp() {
    config = new Config();
  }

  @Test
  public void shouldSetDefaultValues() {
    executorConfig = new ExecutorConfig(config);

    assertThat(executorConfig.getDatabaseUrl()).isEqualTo(DEFAULT_DB_URL);
    assertThat(executorConfig.getDatabaseName()).isEqualTo(DEFAULT_DB_NAME);
    assertThat(executorConfig.getDatabaseUrlOptions()).isEqualTo(EMPTY);
    assertThat(executorConfig.getUsername()).isEqualTo(DEFAULT_DB_USERNAME);
    assertThat(executorConfig.getPassword()).isEqualTo(DEFAULT_DB_PASSWORD);
    assertThat(executorConfig.getDelay()).isEqualTo(DEFAULT_DELAY);
    assertThat(executorConfig.getExecutors()).isEqualTo(DEFAULT_EXECUTORS);
    assertThat(executorConfig.isPickOwnHostOnly()).isEqualTo(true);
    assertThat(executorConfig.getLooseThreshold()).isEqualTo(LOOSE_DEFAULT);
    assertThat(executorConfig.getPackedThreshold()).isEqualTo(PACKED_DEFAULT);
    assertThat(executorConfig.getRepositoriesPath()).isEqualTo(DEFAULT_REPOS_PATH);
    assertThat(executorConfig.getInterval()).isEqualTo(DEFAULT_INTERVAL);
    assertThat(executorConfig.getInitialDelay()).isEqualTo(DEFAULT_INITIAL_DELAY);
  }

  @Test
  public void shouldReadValuesFromConfig() {
    config.setString(DB_SECTION, null, DB_URL_KEY, CUSTOM_DB_URL);
    config.setString(DB_SECTION, null, DB_NAME_KEY, CUSTOM_DB_NAME);
    config.setString(DB_SECTION, null, DB_URL_OPTIONS_KEY, URL_OPTIONS);
    config.setString(DB_SECTION, null, DB_USERNAME_KEY, CUSTOM_DB_USER);
    config.setString(DB_SECTION, null, DB_PASS_KEY, CUSTOM_DB_PASS);
    config.setInt(CORE_SECTION, null, DELAY_KEY, CUSTOM_DELAY);
    config.setInt(CORE_SECTION, null, EXECUTOR_KEY, CUSTOM_EXECUTORS);
    config.setBoolean(CORE_SECTION, null, PICK_OWN_HOST_KEY, false);
    config.setInt(EVALUATION_SECTION, null, LOOSE_KEY, CUSTOM_LOOSE);
    config.setInt(EVALUATION_SECTION, null, PACKED_KEY, CUSTOM_PACKED);
    config.setString(EVALUATION_SECTION, null, REPOS_PATH_KEY, CUSTOM_REPOS_PATH);
    config.setString(EVALUATION_SECTION, null, INTERVAL_KEY, "1 day");
    config.setString(EVALUATION_SECTION, null, START_TIME_KEY, "Sun 00:00");
    executorConfig = new ExecutorConfig(config);

    assertThat(executorConfig.getDatabaseUrl()).isEqualTo(CUSTOM_DB_URL);
    assertThat(executorConfig.getDatabaseName()).isEqualTo(CUSTOM_DB_NAME);
    assertThat(executorConfig.getDatabaseUrlOptions()).isEqualTo(URL_OPTIONS);
    assertThat(executorConfig.getUsername()).isEqualTo(CUSTOM_DB_USER);
    assertThat(executorConfig.getPassword()).isEqualTo(CUSTOM_DB_PASS);
    assertThat(executorConfig.getDelay()).isEqualTo(CUSTOM_DELAY);
    assertThat(executorConfig.getExecutors()).isEqualTo(CUSTOM_EXECUTORS);
    assertThat(executorConfig.isPickOwnHostOnly()).isEqualTo(false);
    assertThat(executorConfig.getLooseThreshold()).isEqualTo(CUSTOM_LOOSE);
    assertThat(executorConfig.getPackedThreshold()).isEqualTo(CUSTOM_PACKED);
    assertThat(executorConfig.getRepositoriesPath()).isEqualTo(CUSTOM_REPOS_PATH);
    assertThat(executorConfig.getInterval()).isEqualTo(ONE_DAY_AS_MS);
    assertThat(executorConfig.getInitialDelay()).isAtLeast(1L);
  }

  @Test
  public void shouldParseTimeOnly() {
    config.setString(EVALUATION_SECTION, null, INTERVAL_KEY, "1 hour");
    config.setString(EVALUATION_SECTION, null, START_TIME_KEY, "15:00");
    executorConfig = new ExecutorConfig(config);

    assertThat(executorConfig.getInitialDelay()).isAtLeast(1L);
  }

  @Test
  public void shouldUseDefaultValuesIfConfigInvalid() {
    config.setString(EVALUATION_SECTION, null, INTERVAL_KEY, "1 x");
    config.setString(EVALUATION_SECTION, null, START_TIME_KEY, "123 ab:cd");
    executorConfig = new ExecutorConfig(config);

    assertThat(executorConfig.getInterval()).isEqualTo(DEFAULT_INTERVAL);
    assertThat(executorConfig.getInitialDelay()).isEqualTo(DEFAULT_INITIAL_DELAY);
  }

  @Test
  public void checkInitialDelayGivesExpectedTime() {
    assertThat(initialDelayFor("11:00", "1h")).isEqualTo(ms(1, HOURS));
    assertThat(initialDelayFor("05:30", "1h")).isEqualTo(ms(30, MINUTES));
    assertThat(initialDelayFor("13:59", "1h")).isEqualTo(ms(59, MINUTES));

    assertThat(initialDelayFor("11:00", "1d")).isEqualTo(ms(1, HOURS));
    assertThat(initialDelayFor("05:30", "1d")).isEqualTo(ms(19, HOURS) + ms(30, MINUTES));
    assertThat(initialDelayFor("Sat 10:00", "1d")).isEqualTo(ms(1, DAYS));
    assertThat(initialDelayFor("Mon 09:00", "1d")).isEqualTo(ms(23, HOURS));

    assertThat(initialDelayFor("11:00", "1w")).isEqualTo(ms(1, HOURS));
    assertThat(initialDelayFor("05:30", "1w"))
        .isEqualTo(ms(7, DAYS) - ms(4, HOURS) - ms(30, MINUTES));
    assertThat(initialDelayFor("Fri 11:00", "1w")).isEqualTo(ms(1, HOURS));
    assertThat(initialDelayFor("Mon 11:00", "1w")).isEqualTo(ms(3, DAYS) + ms(1, HOURS));
  }

  private long initialDelayFor(String startTime, String interval) {
    config.setString(EVALUATION_SECTION, null, INTERVAL_KEY, interval);
    config.setString(EVALUATION_SECTION, null, START_TIME_KEY, startTime);
    executorConfig = new ExecutorConfig(config);
    return executorConfig.initialDelay(startTime, NOW, executorConfig.getInterval());
  }

  private long ms(int cnt, TimeUnit unit) {
    return MILLISECONDS.convert(cnt, unit);
  }
}
