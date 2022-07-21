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

import com.google.gerrit.scenarios.ProjectSimulation
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CheckProjectStatisticsUpToGc extends ProjectSimulation {
  private val data: FeederBuilder = jsonFile(resource).convert(keys).circular
  lazy val MaxSecondsForGcToComplete = 20
  val ChecksPerSecond = 4

  override def relativeRuntimeWeight: Int = MaxSecondsForGcToComplete / SecondsPerWeightUnit

  def this(projectName: String) {
    this()
    this.projectName = projectName
  }

  val test: ScenarioBuilder = scenario(uniqueName)
    .feed(data)
    .exec(http(uniqueName).get("${url}")
      .check(regex("\"number_of_loose_objects\":(\\d+),")
        .is("0")))

  setUp(
    test.inject(
      constantUsersPerSec(ChecksPerSecond) during (MaxSecondsForGcToComplete seconds),
    )).protocols(httpProtocol)
}
