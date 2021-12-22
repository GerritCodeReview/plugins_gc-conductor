// Copyright (C) 2016 The Android Open Source Project
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

import com.ericsson.gerrit.plugins.gcconductor.CommonConfig;
import com.google.common.base.Strings;
import com.google.gerrit.server.config.ConfigUtil;
import com.google.gerrit.server.config.PluginConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;

/** Holds configuration values of the plugin. */
@Singleton
public class EvaluatorConfig extends CommonConfig {
  static final String THREAD_POOL_KEY = "threadPoolSize";
  static final String EXPIRE_TIME_RECHECK_KEY = "expireTimeRecheck";

  static final int THREAD_POOL_DEFAULT = 4;
  static final String EXPIRE_TIME_RECHECK_DEFAULT = "60s";

  private final int threadPoolSize;
  private final long expireTimeRecheck;

  @Inject
  EvaluatorConfig(PluginConfig cfg) {
    super(
        cfg.getString(DB_URL_KEY, DEFAULT_DB_URL),
        cfg.getString(DB_NAME_KEY, DEFAULT_DB_NAME),
        Strings.nullToEmpty(cfg.getString(DB_URL_OPTIONS_KEY)),
        cfg.getString(DB_USERNAME_KEY, DEFAULT_DB_USERNAME),
        cfg.getString(DB_PASS_KEY, DEFAULT_DB_PASSWORD),
        cfg.getInt(PACKED_KEY, PACKED_DEFAULT),
        cfg.getInt(LOOSE_KEY, LOOSE_DEFAULT),
        cfg.getBoolean(GCMODE_KEY, false));
    threadPoolSize = cfg.getInt(THREAD_POOL_KEY, THREAD_POOL_DEFAULT);

    String expireTimeRecheckString =
        cfg.getString(EXPIRE_TIME_RECHECK_KEY, EXPIRE_TIME_RECHECK_DEFAULT);
    expireTimeRecheck = ConfigUtil.getTimeUnit(expireTimeRecheckString, -1, TimeUnit.MILLISECONDS);
  }

  /** @return the number of threads to use for the plugin evaluation tasks. */
  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  /** @return the time to wait before re-evaluating the same repository. */
  public long getExpireTimeRecheck() {
    return expireTimeRecheck;
  }
}
