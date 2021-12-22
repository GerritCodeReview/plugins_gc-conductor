// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License"),
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

package com.ericsson.gerrit.plugins.gcconductor;

import com.google.common.base.Strings;
import org.eclipse.jgit.lib.Config;

/** Database configuration parameters. */
public abstract class CommonConfig {

  public static final String DB_URL_KEY = "databaseUrl";
  public static final String DB_NAME_KEY = "databaseName";
  public static final String DB_URL_OPTIONS_KEY = "databaseUrlOptions";
  public static final String DB_USERNAME_KEY = "username";
  public static final String DB_PASS_KEY = "password";
  public static final String PACKED_KEY = "packed";
  public static final String LOOSE_KEY = "loose";
  public static final String GC_FORCE_AGGRESSIVE_KEY = "forceAggressive";

  public static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/";
  public static final String DEFAULT_DB_NAME = "gc";
  public static final String DEFAULT_DB_USERNAME = DEFAULT_DB_NAME;
  public static final String DEFAULT_DB_PASSWORD = DEFAULT_DB_NAME;
  public static final int PACKED_DEFAULT = 40;
  public static final int LOOSE_DEFAULT = 400;
  public static final boolean DEFAULT_GC_FORCE_AGGRESSIVE = false;

  private final String databaseUrl;
  private final String databaseName;
  private final String databaseUrlOptions;
  private final String username;
  private final String password;
  private final int packed;
  private final int loose;
  private final boolean aggressive;

  /**
   * Create CommonConfig from the specified parameters.
   *
   * @param databaseUrl The database server URL.
   * @param databaseName The database name.
   * @param databaseUrlOptions The database URL options.
   * @param username The database server username.
   * @param password The password of the database server user.
   * @param loose The number of loose objects to consider a repo dirty
   * @param packed The number of packs to consider a repo dirty
   * @param aggressive default gc mode aggressive or not.
   */
  public CommonConfig(
      String databaseUrl,
      String databaseName,
      String databaseUrlOptions,
      String username,
      String password,
      int packed,
      int loose,
      boolean aggressive) {
    this.databaseUrl = databaseUrl.replaceFirst("/?$", "/");
    this.databaseName = databaseName;
    this.databaseUrlOptions = databaseUrlOptions;
    this.username = username;
    this.password = password;
    this.packed = packed;
    this.loose = loose;
    this.aggressive = aggressive;
  }

  /** @return the database server URL. */
  public String getDatabaseUrl() {
    return databaseUrl;
  }

  /** @return the database name. */
  public String getDatabaseName() {
    return databaseName;
  }

  /** @return the database URL options or empty if none. */
  public String getDatabaseUrlOptions() {
    return databaseUrlOptions;
  }

  /** @return the database server username. */
  public String getUsername() {
    return username;
  }

  /** @return the password of the database server user. */
  public String getPassword() {
    return password;
  }

  /** @return the number of pack file threshold. */
  public int getPackedThreshold() {
    return packed;
  }

  /** @return the number of loose objects threshold. */
  public int getLooseThreshold() {
    return loose;
  }

  /** @return if gc mode is set to aggressive, by default its not aggressive */
  public boolean isAggressive() {
    return aggressive;
  }

  protected static String getString(
      Config config, String section, String subsection, String key, String defaultValue) {
    String value = config.getString(section, subsection, key);
    return Strings.isNullOrEmpty(value) ? defaultValue : value;
  }
}
