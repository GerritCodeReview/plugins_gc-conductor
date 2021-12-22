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

package com.ericsson.gerrit.plugins.gcconductor.postgresqueue;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import org.apache.commons.dbcp.BasicDataSource;

final class DatabaseConstants {

  static final String DRIVER = "org.postgresql.Driver";
  static final String INITIAL_DATABASE = "postgres";

  private static final String REPOSITORIES_TABLE = "repositories";
  static final String REPOSITORY = "repository";
  static final String EXECUTOR = "executor";
  private static final String SEQUENCE = "sequence";
  static final String QUEUED_AT = "queued_at";
  static final String HOSTNAME = "hostname";
  static final String AGGRESSIVE = "aggressive"; //TODO: UPGRADE SCHEMA ADD BUMP OR HOST NAME

  static final String CREATE_OR_UPDATE_SCHEMA =
      "DO"
          + " $$"
          + " BEGIN"
          + " CREATE TABLE IF NOT EXISTS "
          + REPOSITORIES_TABLE
          + "("
          + REPOSITORY
          + " VARCHAR(4096) PRIMARY KEY, "
          + SEQUENCE
          + " BIGSERIAL UNIQUE, "
          + QUEUED_AT
          + " TIMESTAMP WITHOUT TIME ZONE DEFAULT localtimestamp, "
          + EXECUTOR
          + " VARCHAR(258), "
          + HOSTNAME
          + " VARCHAR(255) NOT NULL, "
          + AGGRESSIVE
          + "BOOLEAN NOT NULL"
          + ");"
          // This section is temporary to support migrating live, next version will
          // drop the executor tables, only drop the foreign key for now.
          + " IF EXISTS ("
          + "     SELECT constraint_name FROM information_schema.table_constraints"
          + "     WHERE table_name='"
          + REPOSITORIES_TABLE
          + "'"
          + "     AND constraint_name='repositories_executor_fkey')"
          + " THEN"
          + "     ALTER TABLE "
          + REPOSITORIES_TABLE
          + " DROP CONSTRAINT repositories_executor_fkey;"
          + " END IF;"
          + " END "
          + " $$";

  private static final String SELECT_REPOSITORIES = "SELECT * FROM " + REPOSITORIES_TABLE;

  static final String SELECT_REPOSITORIES_ORDERED = SELECT_REPOSITORIES + " ORDER BY " + SEQUENCE;

  private DatabaseConstants() {}

  static final String databaseExists(String name) {
    return "SELECT 1 from pg_database WHERE datname='" + name + "'";
  }

  static final String createDatabase(String name) {
    return "CREATE DATABASE " + name;
  }

  static final String dropDatabase(String name) {
    return "DROP DATABASE " + name;
  }

  static final String select(String repository) {
    return SELECT_REPOSITORIES + " WHERE " + REPOSITORY + "='" + repository + "'";
  }

  static final String insert(String repository, String hostname) {
    return format(
        "INSERT INTO %s (%s,%s) SELECT '%s','%s' WHERE NOT EXISTS (%s)",
        REPOSITORIES_TABLE, REPOSITORY, HOSTNAME, repository, hostname, select(repository));
    //TODO UPGRADE WITH AGGRESSIVE info
  }

  static final String delete(String repository) {
    return "DELETE FROM " + REPOSITORIES_TABLE + " WHERE " + REPOSITORY + "='" + repository + "'";
  }

  static final String updateExecutor(
      String executor, long queuedForLongerThan, Optional<String> queuedFrom) {
    return format(
        "UPDATE %s SET %s='%s' WHERE %s IN (SELECT %s FROM %s WHERE (%s is null OR %s='' OR %s='%s') AND age(localtimestamp, queued_at) > '%s' %s ORDER BY %s LIMIT 1 FOR UPDATE OF %s) RETURNING *",
        REPOSITORIES_TABLE,
        EXECUTOR,
        executor,
        REPOSITORY,
        REPOSITORY,
        REPOSITORIES_TABLE,
        EXECUTOR,
        EXECUTOR,
        EXECUTOR,
        executor,
        queuedForLongerThan,
        (queuedFrom.isPresent() ? " AND '" + queuedFrom.get() + "' LIKE hostname||'%'" : ""),
        SEQUENCE,
        REPOSITORIES_TABLE);
  }

  static final String updateQueuedFrom(String hostname) {
    return format(
        "UPDATE %s SET %s='%s' WHERE %s is NULL", REPOSITORIES_TABLE, HOSTNAME, hostname, EXECUTOR);
  }

  static final String clearExecutor(String repository) {
    return format(
        "UPDATE %s SET %s=NULL WHERE %s='%s'",
        REPOSITORIES_TABLE, EXECUTOR, REPOSITORY, repository);
  }

  static final String bumpRepository(String repository) {
    return format(
        "UPDATE %s SET %s=(SELECT min(%s) FROM %s) -1 WHERE %s='%s'",
        REPOSITORIES_TABLE, SEQUENCE, SEQUENCE, REPOSITORIES_TABLE, REPOSITORY, repository);
  }

  static boolean executeStatement(BasicDataSource ds, String query) throws SQLException {
    try (Connection conn = ds.getConnection();
        Statement stat = conn.createStatement()) {
      return stat.execute(query);
    }
  }
}
