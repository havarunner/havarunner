package com.github.havarunner

import java.lang.reflect.Method
import com.github.havarunner.TestInstanceCache._

private[havarunner] case class TestAndParameters(
  testMethod: Method,
  testClass: Class[_],
  outerClass: Option[Class[_]],
  outerTest: Option[TestAndParameters],
  expectedException: Option[Class[_<:Throwable]],
  scenario: Option[AnyRef],
  partOf: Option[HavaRunnerSuite[_]],
  ignored: Boolean,
  testContext: TestContext,
  afterAll: Seq[Method],
  runSequentially: Boolean
) {
  val scenarioAndClass = ScenarioAndClass(testClass, scenario)

  val scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])