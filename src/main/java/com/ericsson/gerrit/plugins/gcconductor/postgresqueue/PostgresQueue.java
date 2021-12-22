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

import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.AGGRESSIVE;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.CREATE_OR_UPDATE_SCHEMA;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.EXECUTOR;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.HOSTNAME;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.QUEUED_AT;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.REPOSITORY;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.SELECT_REPOSITORIES_ORDERED;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.bumpRepository;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.clearExecutor;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.delete;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.executeStatement;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.insert;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.select;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.updateExecutor;
import static com.ericsson.gerrit.plugins.gcconductor.postgresqueue.DatabaseConstants.updateQueuedFrom;

import com.ericsson.gerrit.plugins.gcconductor.GcQueue;
import com.ericsson.gerrit.plugins.gcconductor.GcQueueException;
import com.ericsson.gerrit.plugins.gcconductor.RepositoryInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.dbcp.BasicDataSource;

@Singleton
public class PostgresQueue implements GcQueue {

  private final BasicDataSource dataSource;

  @Inject
  public PostgresQueue(BasicDataSource dataSource) throws SQLException {
    this.dataSource = dataSource;
    executeStatement(dataSource, CREATE_OR_UPDATE_SCHEMA);
  }

  @Override
  public void add(String repository, String queuedFrom, boolean isAggressive) throws GcQueueException {
    try {
      executeStatement(dataSource, insert(repository, queuedFrom, isAggressive));
    } catch (SQLException e) {
      if (!"23505".equals(e.getSQLState())) {
        // UNIQUE CONSTRAINT violation means repository is already in the queue
        throw new GcQueueException("Failed to add repository " + repository, e);
      }
    }
  }

  @Override
  public RepositoryInfo pick(String executor, long queuedForLongerThan, Optional<String> queuedFrom)
      throws GcQueueException {
    try (Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        ResultSet resultSet =
            stat.executeQuery(updateExecutor(executor, queuedForLongerThan, queuedFrom))) {
      if (resultSet.next()) {
        return toRepositoryInfo(resultSet);
      }
    } catch (SQLException e) {
      throw new GcQueueException("Failed to pick repository", e);
    }
    return null;
  }

  @Override
  public void unpick(String repository) throws GcQueueException {
    try {
      executeStatement(dataSource, clearExecutor(repository));
    } catch (SQLException e) {
      throw new GcQueueException("Failed to unpick repository " + repository, e);
    }
  }

  @Override
  public void remove(String repository) throws GcQueueException {
    try {
      executeStatement(dataSource, delete(repository));
    } catch (SQLException e) {
      throw new GcQueueException("Failed to remove repository " + repository, e);
    }
  }

  @Override
  public boolean contains(String repository) throws GcQueueException {
    try (Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        ResultSet resultSet = stat.executeQuery(select(repository))) {
      if (resultSet.next()) {
        return true;
      }
    } catch (SQLException e) {
      throw new GcQueueException("Failed to check if queue contains repository " + repository, e);
    }
    return false;
  }

  @Override
  public void resetQueuedFrom(String queuedFrom) throws GcQueueException {
    try {
      executeStatement(dataSource, updateQueuedFrom(queuedFrom));
    } catch (SQLException e) {
      throw new GcQueueException("Failed to reset queuedFrom", e);
    }
  }

  @Override
  public List<RepositoryInfo> list() throws GcQueueException {
    try (Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        ResultSet resultSet = stat.executeQuery(SELECT_REPOSITORIES_ORDERED)) {
      List<RepositoryInfo> repositories = new ArrayList<>();
      while (resultSet.next()) {
        repositories.add(toRepositoryInfo(resultSet));
      }
      return repositories;
    } catch (SQLException e) {
      throw new GcQueueException("Failed to list repositories ", e);
    }
  }

  @Override
  public void bumpToFirst(String repository) throws GcQueueException {
    try {
      executeStatement(dataSource, bumpRepository(repository));
    } catch (SQLException e) {
      throw new GcQueueException("Failed to update repository priority ", e);
    }
  }

  private RepositoryInfo toRepositoryInfo(ResultSet resultSet) throws SQLException {
    int repositoryColumn = resultSet.findColumn(REPOSITORY);
    int queuedAtColumn = resultSet.findColumn(QUEUED_AT);
    int executorColumn = resultSet.findColumn(EXECUTOR);
    int hostnameColumn = resultSet.findColumn(HOSTNAME);
    int aggressiveColumn = resultSet.findColumn(AGGRESSIVE);
    return new RepositoryInfo(
        resultSet.getString(repositoryColumn),
        resultSet.getTimestamp(queuedAtColumn),
        resultSet.getString(executorColumn),
        resultSet.getString(hostnameColumn),
        resultSet.getBoolean(aggressiveColumn));
  }
}
