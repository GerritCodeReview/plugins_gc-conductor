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

import com.google.gerrit.scenarios.GerritSimulation
import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.http

class DeleteChangesAfterGc extends GerritSimulation {
  private val data: FeederBuilder = jsonFile(resource).convert(keys).circular
  private val numberKey = "number"
  var upToNumber = 1

  private lazy val OrderOfChangesToDelete = 200
  private lazy val PerSecond = 4
  private lazy val secondsToDeleted = OrderOfChangesToDelete / PerSecond

  override def relativeRuntimeWeight: Int = (secondsToDeleted + SecondsPerWeightUnit) / SecondsPerWeightUnit

  val test: ScenarioBuilder = scenario(unique)
    .feed(data)
    .exec(session => {
      val numbered: Session = session.set(numberKey, upToNumber)
      upToNumber -= 1
      numbered
    })
    .exec(http(unique).delete("${url}${" + numberKey + "}"))
}
