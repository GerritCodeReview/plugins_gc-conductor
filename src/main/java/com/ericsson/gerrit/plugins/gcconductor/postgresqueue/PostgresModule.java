// Copyright (C) 2017 Ericsson
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
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.createDatabase;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.databaseExists;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.executeStatement;

import com.ericsson.gerrit.plugins.gcconductor.CommonConfig;
import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.ShutdownListener;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.internal.UniqueAnnotations;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp.BasicDataSource;

/** Configures bindings of the Postgres queue implementation. */
public class PostgresModule extends AbstractModule {

  private static final long EVICT_IDLE_TIME_MS = 1000L * 60;
  private Class<? extends CommonConfig> commonConfig;

  public PostgresModule(Class<? extends CommonConfig> commonConfig) {
    this.commonConfig = commonConfig;
  }

  @Override
  protected void configure() {
    bind(CommonConfig.class).to(commonConfig);
    bind(GcQueue.class).to(PostgresQueue.class);
    bind(ShutdownListener.class)
        .annotatedWith(UniqueAnnotations.create())
        .to(DatabaseAccessCleanUp.class);
  }

  /**
   * Provide access to the gc database. Database will be created if it does not exist.
   *
   * @param cfg The database configuration
   * @return BasicDataSource to access gc database.
   * @throws SQLException If an error occur while creating the database.
   */
  @Provides
  @Singleton
  BasicDataSource provideGcDatabaseAccess(CommonConfig cfg) throws SQLException {
    BasicDataSource adminDataSource = createDataSource(cfg, INITIAL_DATABASE);
    try (Connection conn = adminDataSource.getConnection();
        Statement stat = conn.createStatement();
        ResultSet resultSet = stat.executeQuery(databaseExists(cfg.getDatabaseName()))) {
      if (!resultSet.next()) {
        executeStatement(adminDataSource, createDatabase(cfg.getDatabaseName()));
      }
    } finally {
      adminDataSource.close();
    }
    return createDataSource(cfg, cfg.getDatabaseName());
  }

  private static BasicDataSource createDataSource(CommonConfig cfg, String database) {
    BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(DRIVER);
    ds.setUrl(cfg.getDatabaseUrl() + database + cfg.getDatabaseUrlOptions());
    ds.setUsername(cfg.getUsername());
    ds.setPassword(cfg.getPassword());
    ds.setMinEvictableIdleTimeMillis(EVICT_IDLE_TIME_MS);
    ds.setTimeBetweenEvictionRunsMillis(EVICT_IDLE_TIME_MS / 2);
    return ds;
  }

  /** Close the database access when plugin is unloaded */
  @VisibleForTesting
  static class DatabaseAccessCleanUp implements ShutdownListener {
    private final BasicDataSource dataSource;

    @Inject
    public DatabaseAccessCleanUp(BasicDataSource dataSource) {
      this.dataSource = dataSource;
    }

    @Override
    public void onShutdown() {
      try {
        dataSource.close();
      } catch (SQLException e) {
        throw new RuntimeException("Failed to close database connection: " + e.getMessage(), e);
      }
    }
  }
}
