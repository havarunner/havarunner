package com.github.havarunner

import java.lang.reflect.{Field, Method}

private[havarunner] case class TestAndParameters(
  testMethod: Method,
  testClass: Class[_],
  rules: Seq[Field],
  expectedException: Option[Class[_<:Throwable]],
  timeout: Option[Long],
  scenario: Option[AnyRef],
  partOf: Option[HavaRunnerSuite[_]],
  ignored: Boolean,
  testContext: TestContext,
  afterAll: Seq[Method],
  runSequentially: Boolean
) extends MaybeSequential {

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

private[havarunner] trait MaybeSequential {
  def runSequentially: Boolean
}