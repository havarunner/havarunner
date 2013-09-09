package havarunner

import org.junit.runners.model.{FrameworkMethod, TestClass}
import java.lang.reflect.Method
import org.junit.{Test, Assert, Before}
import havarunner.ScenarioHelper._
import scala.collection.JavaConversions._

private[havarunner] object HavaRunnerHelper {
  def toTestParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    classesToTest.flatMap(aClass => {
      val testClass = new TestClass(aClass)
      findTestMethods(testClass).map(methodAndScenario => {
        new TestAndParameters(
          new FrameworkMethod(methodAndScenario.method),
          testClass,
          methodAndScenario.scenario.asInstanceOf[Object],
          findBefores(testClass)
        )
      })
    })

  }

  def findBefores(testClass: TestClass): List[Method] =
    testClass.getJavaClass.getDeclaredMethods.filter(method =>
      method.getAnnotation(classOf[Before]) != null
    ).toList

  def findOnlyConstructor(testClass: TestClass) = {
    val declaredConstructors = testClass.getJavaClass.getDeclaredConstructors
    Assert.assertEquals(
      String.format("The class %s should have exactly one no-arg constructor", testClass.getJavaClass.getName),
      1,
      declaredConstructors.length
    )
    val declaredConstructor = declaredConstructors.head
    declaredConstructor.setAccessible(true)
    declaredConstructor
  }

  def isScenarioClass(clazz: Class[_ <: Any]) = classOf[TestWithMultipleScenarios[A]].isAssignableFrom(clazz)

  private def findTestMethods(testClass: TestClass): Seq[MethodAndScenario] = {
    scenarios(testClass).flatMap(scenario => {
      val testMethods = testClass.getJavaClass.getDeclaredMethods.filter(_.getAnnotation(classOf[Test]) != null)
      testMethods.map(testMethod => {
        testMethod.setAccessible(true)
        new MethodAndScenario(scenario, testMethod)
      })
    })
  }

  private def scenarios(testClass: TestClass): Seq[Any] = {
    if (isScenarioClass(testClass.getJavaClass)) {
      newTestClassInstance(testClass).asInstanceOf[TestWithMultipleScenarios[A]].scenarios.toList
    } else {
      Seq(defaultScenario)
    }
  }

  private def newTestClassInstance(testClass: TestClass) = findOnlyConstructor(testClass).newInstance()

  private class MethodAndScenario(val scenario: Any, val method: Method)

  type A = Any
}
