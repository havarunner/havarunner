package com.github.havarunner

import java.lang.reflect.{Field, Method}
import com.github.havarunner.annotation.RunSequentially

private[havarunner] case class TestAndParameters(
  testMethod: Method,
  testClass: Class[_],
  rules: Seq[Field],
  expectedException: Option[Class[_<:Throwable]],
  timeout: Option[Long],
  scenario: Option[AnyRef],
  partOf: Option[Class[_ <:HavaRunnerSuite[_]]],
  ignored: Boolean,
  afterAll: Seq[Method],
  after: Seq[Method],
  before: Seq[Method],
  runSequentially: Option[RunSequentially]
) extends MaybeSequential with InstanceGroup[ScenarioAndClass] {

  val groupCriterion = ScenarioAndClass(testClass, scenario)
  
  lazy val scenarioToString = scenario.fold("")(scenario => s" (when ${scenario.toString})")

  override def toString =
    String.format(
      "%s#%s%s%s",
      testClass.getName,
      testMethod.getName,
      scenarioToString,
      partOf.fold("")(suite => s" (suite $suite)")
    )

  lazy val toStringWithoutSuite =
    testClass.getSimpleName + "#" + testMethod.getName + scenarioToString
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])

private[havarunner] case class TestInstance(instance: Any)

private[havarunner] trait MaybeSequential {
  def runSequentially: Option[RunSequentially]
}

private[havarunner] trait InstanceGroup[T] {
  /**
   * The object by which the test instances should be grouped
   */
  def groupCriterion: T
}