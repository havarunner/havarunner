package com.github.havarunner

import org.junit.{Ignore, Test}
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import java.lang.reflect.Method
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath
import java.util.logging.{Level, Logger}

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    localAndSuiteTests(classesToTest).flatMap((testClassAndSource: TestClassAndSource) => {
      findTestMethods(testClassAndSource.testClass).map(methodAndScenario => {
        new TestAndParameters(
          methodAndScenario.method,
          testClassAndSource.testClass,
          ignored = methodAndScenario.method.getAnnotation(classOf[Ignore]) != null || isAnnotatedWith(testClassAndSource.testClass, classOf[Ignore]),
          expectedException = expectedException(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          testContext = testClassAndSource.testContext,
          afterAll = findMethods(testClassAndSource.testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          runSequentially = classesToTest.exists(isAnnotatedWith(_, classOf[RunSequentially]))
        )
      })
    })
  }

  private def localAndSuiteTests(classesToTest: Seq[Class[_ <: Any]]): Seq[TestClassAndSource] = {
    val nonSuiteTests = classesToTest.map(TestClassAndSource(_))
    val suiteTests = classesToTest.flatMap(classToTest =>
      findSuiteMembers(classToTest).map(suiteMember => TestClassAndSource(suiteMember, SuiteContext(classToTest)))
    )
    nonSuiteTests ++ suiteTests
  }

  private def expectedException(method: Method): Option[Class[_ <: Throwable]] = {
    val expected = method.getAnnotation(classOf[Test]).expected()
    if (expected == classOf[org.junit.Test.None])
      None
    else
      Some(expected)
  }

  private def isScenarioClass(clazz: Class[_]) = scenarioMethod(clazz).isDefined

  private def scenarioMethod(clazz: Class[_]): Option[Method] = findMethods(clazz, classOf[Scenarios]).headOption.map(method => { method.setAccessible(true); method })

  private def findTestMethods(testClass: Class[_]): Seq[MethodAndScenario] = {
    val testMethods = findMethods(testClass, classOf[Test]).map(method => { method.setAccessible(true); method })
    scenarios(testClass) match {
      case Some(scenarios) =>
        scenarios.flatMap(scenario =>
          testMethods.map(new MethodAndScenario(Some(scenario), _))
        )
      case None =>
        testMethods.map(new MethodAndScenario(None, _))
    }
  }

  private def findSuiteMembers(testClass: Class[_]): Seq[Class[_]] =
    if (classOf[HavaRunnerSuite[_]].isAssignableFrom(testClass))
      suiteMembers(testClass).filter(_.getAnnotation(classOf[PartOf]).value() == testClass)
    else
      Nil

  private def suiteMembers(testClass: Class[_]): Seq[Class[_]] = {
    val maybeLoadedClasses: Seq[Option[Class[_]]] =
      ClassPath.
        from(getClass.getClassLoader).
        getTopLevelClassesRecursive(testClass.getPackage.getName). // Use a restricting prefix. Otherwise we would load all the classes in the classpath.
        toSeq.
        map(classInfo =>
          try {
            Some(classInfo.load)
          } catch {
            case e: java.lang.NoClassDefFoundError =>
              Logger.getLogger(getClass.getName).log(
                Level.FINE,
                "While scanning for test classes, HavaRunner encountered a potential problem: " + e.getMessage
              )
              None
          }
        )
    val loadedClasses: Seq[Class[_]] = maybeLoadedClasses.flatMap(identity(_))
    loadedClasses.filter(_.isAnnotationPresent(classOf[PartOf]))
  }


  private def scenarios(testClass: Class[_]): Option[Seq[AnyRef]] = {
    if (isScenarioClass(testClass)) {
      val scenarios = scenarioMethod(testClass).get.invoke(null).asInstanceOf[java.lang.Iterable[A]]
      Some(scenarios.iterator().toSeq)
    } else {
      None
    }
  }

  private class MethodAndScenario(val scenario: Option[AnyRef], val method: Method)


  private type A = AnyRef
}

private[havarunner] case class TestClassAndSource(testClass: Class[_], testContext: TestContext = DefaultContext)
private[havarunner] trait TestContext
private[havarunner] case class SuiteContext(suiteClass: Class[_]) extends TestContext
private[havarunner] case object DefaultContext extends TestContext