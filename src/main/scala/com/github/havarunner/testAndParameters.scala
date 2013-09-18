package com.github.havarunner

import java.lang.reflect.Method
import com.github.havarunner.TestInstanceCache._

private[havarunner] class TestAndParameters(
  val testMethod: Method,
  val testClass: Class[_],
  val expectedException: Option[Class[_<:Throwable]],
  val scenario: Option[AnyRef],
  val partOf: Option[HavaRunnerSuite[_]],
  val ignored: Boolean,
  val testContext: TestContext,
  val afterAll: Seq[Method],
  val runSequentially: Boolean
) {

  val scenarioAndClass = ScenarioAndClass(testClass, scenario)

  lazy val testInstance = fromTestInstanceCache(this)

  def scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])