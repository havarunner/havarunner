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

  def scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")

  override def toString =
    String.format(
      "%s#%s%s%s",
      testClass.getName,
      testMethod.getName,
      scenario.map(scen => s" (scenario $scen)").getOrElse(""),
      partOf.map(suite => s" (suite $suite)").getOrElse("")
    )
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])