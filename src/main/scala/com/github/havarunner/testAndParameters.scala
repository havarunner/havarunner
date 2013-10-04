package com.github.havarunner

import java.lang.reflect.Method
import com.github.havarunner.TestInstanceCache._

private[havarunner] case class TestAndParameters(
  testMethod: Method,
  testClass: Class[_],
  expectedException: Option[Class[_<:Throwable]],
  scenario: Option[AnyRef],
  partOf: Option[HavaRunnerSuite[_]],
  ignored: Boolean,
  testContext: TestContext,
  afterAll: Seq[Method],
  runSequentially: Boolean
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