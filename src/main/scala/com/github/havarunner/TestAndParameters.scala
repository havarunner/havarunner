package com.github.havarunner

import java.lang.reflect.Method
import scala.collection.mutable
import _root_.com.github.havarunner.TestAndParameters._
import _root_.com.github.havarunner.Reflections._
import com.github.havarunner.HavaRunnerSuite._

private[havarunner] class TestAndParameters(
  val testMethod: Method,
  val testClass: Class[_],
  val expectedException: Option[Class[_<:Throwable]],
  val scenario: Option[AnyRef],
  val ignored: Boolean,
  val testContext: TestContext,
  val afterAll: Seq[Method],
  val runSequentially: Boolean
) {

  val scenarioAndClass = ScenarioAndClass(testClass, scenario)

  lazy val testInstance = newOrCachedInstance(scenarioAndClass)

  def scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")
}

private[havarunner] object TestAndParameters {
  val cache = new mutable.HashMap[ScenarioAndClass, Any] with mutable.SynchronizedMap[ScenarioAndClass, Any]

  def newOrCachedInstance(scenarioAndClass: ScenarioAndClass): Any =
    scenarioAndClass.clazz.synchronized {
      cache.get(scenarioAndClass) getOrElse {
        val testInstance = instantiate(suiteOption(scenarioAndClass.clazz), scenarioAndClass.scenarioOption, scenarioAndClass.clazz)
        cache(scenarioAndClass) = testInstance
        testInstance
      }
    }
}

private[havarunner] case class ScenarioAndClass(clazz: Class[_], scenarioOption: Option[AnyRef])