package com.github.havarunner

import org.junit.runners.model.TestClass
import java.lang.reflect.Method
import scala.collection.mutable
import _root_.com.github.havarunner.TestAndParameters._
import com.github.havarunner.exception.ScenarioConstructorNotFound

private[havarunner] class TestAndParameters(
  val testMethod: Method,
  val testClass: TestClass,
  val expectedException: Option[Class[_<:Throwable]],
  val scenario: Option[AnyRef],
  val afters: Seq[Method],
  val runSequentially: Boolean
) {

  lazy val testInstance = newOrCachedInstance(testClass.getJavaClass, scenario)

  def scenarioToString = scenario.map(scenario => s" (when ${scenario.toString})").getOrElse("")
}

private[havarunner] object TestAndParameters {
  val cache = new mutable.HashMap[ScenarioAndClass, Any]()

  def newOrCachedInstance(clazz: Class[_], scenario: Option[AnyRef]): Any = synchronized {
    val key = ScenarioAndClass(clazz, scenario)
    cache.get(key) match {
      case Some(cachedInstance) =>
        cachedInstance
      case None                 =>
        val testInstance = newInstance(clazz, scenario)
        cache(key) = testInstance
        testInstance
    }
  }

  def newInstance(clazz: Class[_], scenario: Option[AnyRef]) = reportMissingScenarioConstructor(clazz, scenario) {
    val constructor = scenario match {
      case Some(scenario) => clazz.getDeclaredConstructor(scenario.getClass)
      case None           => clazz.getDeclaredConstructor()
    }
    constructor.setAccessible(true)
    scenario match {
      case Some(scenario) => constructor.newInstance(scenario)
      case None           => constructor.newInstance()
    }
  }

  def reportMissingScenarioConstructor(clazz: Class[_], scenario: Option[AnyRef])(op: => Any) = {
    try {
      op
    } catch {
      case e: NoSuchMethodException =>
        throw new ScenarioConstructorNotFound(clazz, scenario)
    }
  }

  case class ScenarioAndClass(clazz: Class[_], scenario: Option[Any])
}
