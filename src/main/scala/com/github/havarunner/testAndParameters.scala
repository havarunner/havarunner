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
  after: Seq[Pair[Method, InstantiationParams]] = Nil, // TODO default values are suspicious
  before: Seq[Pair[Method, InstantiationParams]] = Nil, // TODO default values are suspicious
  runSequentially: Option[RunSequentially] = None, // TODO default values are suspicious
  encloser: Option[ScenarioAndClass] = None // TODO default values are suspicious
) extends MaybeSequential with InstantiationParams {

  val groupCriterion = ScenarioAndClass(testClass, scenario) // TODO use the topmost class as the class reference

  lazy val scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")

  override def toString =
    String.format(
      "%s#%s%s%s",
      testClass.getName,
      testMethod.getName,
      scenarioToString,
      partOf.map(suite => s" (suite $suite)").getOrElse("")
    )

  lazy val toStringWithoutSuite =
    testClass.getSimpleName + "#" + testMethod.getName + scenarioToString
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])

private[havarunner] case class TestInstance(instance: Any)

private[havarunner] trait InstantiationParams extends InstanceGroup[ScenarioAndClass] {
  val testClass: Class[_]
  val partOf: Option[Class[_ <:HavaRunnerSuite[_]]]
  val scenario: Option[AnyRef]
  val encloser: Option[ScenarioAndClass]
}

private[havarunner] trait MaybeSequential {
  def runSequentially: Option[RunSequentially]
}

private[havarunner] trait InstanceGroup[T] {
  /**
   * The object by which the test instances should be grouped
   */
  def groupCriterion: T
}