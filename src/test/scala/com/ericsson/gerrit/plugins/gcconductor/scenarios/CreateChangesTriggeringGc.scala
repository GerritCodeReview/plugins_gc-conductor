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

class CreateChangesTriggeringGc extends ProjectSimulation {
  private val data: FeederBuilder = jsonFile(resource).convert(keys).circular

  private lazy val minuteMultiplier = getProperty("minute_multiplier", 1).toInt
  lazy val secondsToNextEvaluation: Int = 60 * minuteMultiplier
  private lazy val looseObjectsToEnqueueGc = getProperty("loose_objects", 400).toInt
  private lazy val LooseObjectsPerChange = 2
  private lazy val ChangesMultiplier = 8
  lazy val changesPerSecond: Int = 4 * ChangesMultiplier
  val ChangesForLastEvaluation: Int = single

  lazy val secondsForLastEvaluation: Int = SecondsPerWeightUnit
  private lazy val changesToEnqueueGc = looseObjectsToEnqueueGc * ChangesMultiplier / LooseObjectsPerChange
  lazy val secondsToChanges: Int = changesToEnqueueGc / changesPerSecond
  private lazy val maxSecondsToEnqueueGc = secondsToChanges + secondsToNextEvaluation + secondsForLastEvaluation

  override def relativeRuntimeWeight: Int = maxSecondsToEnqueueGc / SecondsPerWeightUnit

  def this(projectName: String, deleteChanges: DeleteChangesAfterGc) {
    this()
    this.projectName = projectName
    this.deleteChanges = deleteChanges
  }

  val test: ScenarioBuilder = scenario(uniqueName)
    .feed(data)
    .exec(httpRequest
      .body(ElFileBody(body)).asJson
      .check(regex("\"_" + numberKey + "\":(\\d+),").saveAs(numberKey)))
    .exec(session => {
      deleteChanges.upToNumber = session(numberKey).as[Int]
      session
    })

  private val checkStatsUpToGc = new CheckProjectStatisticsUpToGc(projectName)
  private var deleteChanges = new DeleteChangesAfterGc

  setUp(
    test.inject(
      nothingFor(stepWaitTime(this) seconds),
      constantUsersPerSec(changesPerSecond) during (secondsToChanges seconds),
      nothingFor(secondsToNextEvaluation seconds),
      nothingFor(secondsForLastEvaluation / 2 seconds),
      atOnceUsers(ChangesForLastEvaluation),
      nothingFor(secondsForLastEvaluation / 2 seconds)
    ),
    checkStatsUpToGc.test.inject(
      nothingFor(stepWaitTime(checkStatsUpToGc) seconds),
      constantUsersPerSec(checkStatsUpToGc.ChecksPerSecond) during (checkStatsUpToGc.MaxSecondsForGcToComplete seconds)
    ),
    deleteChanges.test.inject(
      nothingFor(stepWaitTime(deleteChanges) seconds),
      constantUsersPerSec(changesPerSecond) during (secondsToChanges seconds),
      atOnceUsers(ChangesForLastEvaluation)
    ),
  ).protocols(httpProtocol)
}
