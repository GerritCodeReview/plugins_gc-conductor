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
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CreateChangesTriggeringGc extends GerritSimulation {
  private val data: FeederBuilder = jsonFile(resource).convert(keys).circular
  private val default: String = name
  private val numberKey = "_number"

  private lazy val DefaultSecondsToNextEvaluation = 60
  private lazy val DefaultSecondsToNextGcDequeue = 60
  private lazy val DefaultLooseObjectsToEnqueueGc = 400
  private lazy val LooseObjectsPerChange = 2
  private lazy val ChangesPerSecond = 4
  private val ChangesForLastEvaluation = 1

  private lazy val secondsForLastEvaluation = SecondsPerWeightUnit * 2
  private lazy val secondsForLastGcExecution = secondsForLastEvaluation * 2
  private lazy val changesToEnqueueGc = DefaultLooseObjectsToEnqueueGc / LooseObjectsPerChange
  private lazy val secondsToChanges = changesToEnqueueGc / ChangesPerSecond
  private lazy val maxSecondsToEnqueueGc = secondsToChanges + DefaultSecondsToNextEvaluation + secondsForLastEvaluation
  private lazy val maxSecondsToExecuteGc = maxSecondsToEnqueueGc + DefaultSecondsToNextGcDequeue + secondsForLastGcExecution

  override def relativeRuntimeWeight: Int = maxSecondsToExecuteGc / SecondsPerWeightUnit

  private val test: ScenarioBuilder = scenario(unique)
    .feed(data)
    .exec(httpRequest
      .body(ElFileBody(body)).asJson
      .check(regex("\"" + numberKey + "\":(\\d+),").saveAs(numberKey)))
    .exec(session => {
      deleteChanges.upToNumber = session(numberKey).as[Int]
      session
    })

  private val createProject = new CreateProject(default)
  private val checkStatsAfterGc = new CheckProjectStatisticsAfterGc(default)
  private val deleteChanges = new DeleteChangesAfterGc
  private val deleteProject = new DeleteProject(default)

  setUp(
    createProject.test.inject(
      nothingFor(stepWaitTime(createProject) seconds),
      atOnceUsers(1)
    ),
    test.inject(
      nothingFor(stepWaitTime(this) seconds),
      constantUsersPerSec(ChangesPerSecond) during (secondsToChanges seconds),
      nothingFor(DefaultSecondsToNextEvaluation seconds),
      nothingFor(secondsForLastEvaluation / 2 seconds),
      atOnceUsers(ChangesForLastEvaluation),
      nothingFor(secondsForLastEvaluation / 2 seconds)
    ),
    checkStatsAfterGc.test.inject(
      nothingFor(stepWaitTime(checkStatsAfterGc) seconds),
      atOnceUsers(1)
    ),
    deleteChanges.test.inject(
      nothingFor(stepWaitTime(deleteChanges) seconds),
      constantUsersPerSec(ChangesPerSecond) during (secondsToChanges seconds),
      atOnceUsers(ChangesForLastEvaluation)
    ),
    deleteProject.test.inject(
      nothingFor(stepWaitTime(deleteProject) seconds),
      atOnceUsers(1)
    ),
  ).protocols(httpProtocol)
}