// Copyright (C) 2020 The Android Open Source Project
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

package com.ericsson.gerrit.plugins.gcconductor.scenarios

import com.google.gerrit.scenarios._
import io.gatling.core.Predef.{atOnceUsers, nothingFor, _}

import scala.concurrent.duration._

class CreateChangesTriggeringGcWithProject extends GerritSimulation {
  private val projectName = className

  private val createProject = new CreateProject(projectName)
  private val deleteChanges = new DeleteChangesAfterGc
  private val createChanges = new CreateChangesTriggeringGc(projectName, deleteChanges)
  private val checkStatsUpToGc = new CheckProjectStatisticsUpToGc(projectName)
  private val deleteProject = new DeleteProject(projectName)

  setUp(
    createProject.test.inject(
      nothingFor(stepWaitTime(createProject) seconds),
      atOnceUsers(single)
    ),
    createChanges.test.inject(
      nothingFor(stepWaitTime(createChanges) seconds),
      constantUsersPerSec(createChanges.changesPerSecond) during (createChanges.secondsToChanges seconds),
      nothingFor(createChanges.DefaultSecondsToNextEvaluation seconds),
      nothingFor(createChanges.secondsForLastEvaluation / 2 seconds),
      atOnceUsers(createChanges.ChangesForLastEvaluation)
    ),
    checkStatsUpToGc.test.inject(
      nothingFor(stepWaitTime(checkStatsUpToGc) seconds),
      constantUsersPerSec(checkStatsUpToGc.ChecksPerSecond) during (checkStatsUpToGc.MaxSecondsForGcToComplete seconds)
    ),
    deleteChanges.test.inject(
      nothingFor(stepWaitTime(deleteChanges) seconds),
      constantUsersPerSec(createChanges.changesPerSecond) during (createChanges.secondsToChanges seconds),
      atOnceUsers(createChanges.ChangesForLastEvaluation)
    ),
    deleteProject.test.inject(
      nothingFor(stepWaitTime(deleteProject) seconds),
      atOnceUsers(single)
    ),
  ).protocols(httpProtocol)
}
