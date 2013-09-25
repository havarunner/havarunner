package com.github.havarunner

import org.junit.{Ignore, Test}
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import com.github.havarunner.SuiteCache._
import java.lang.reflect.Method
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    localAndSuiteTests(classesToTest).flatMap((testClassAndSource: TestClassAndSource) => {
      findTestMethods(testClassAndSource.testClass).map(methodAndScenario => {
        new TestAndParameters(
          methodAndScenario.method,
          testClassAndSource.testClass,
          ignored = methodAndScenario.method.getAnnotation(classOf[Ignore]) != null || findAnnotationRecursively(testClassAndSource.testClass, classOf[Ignore]).isDefined,
          expectedException = expectedException(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          partOf = suiteOption(testClassAndSource.testClass),
          testContext = testClassAndSource.testContext,
          afterAll = findMethods(testClassAndSource.testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          runSequentially = classesToTest.exists(
            findAnnotationRecursively(_, classOf[RunSequentially]).isDefined || findAnnotationRecursively(testClassAndSource.testClass, classOf[RunSequentially]).isDefined
          )
        )
      })
    })
  }

  private def suiteOption(implicit clazz: Class[_]): Option[HavaRunnerSuite[_]] =
    findAnnotationRecursively(clazz, classOf[PartOf]).
      map(_.asInstanceOf[PartOf]).
      map(_.value()).
      map(fromSuiteInstanceCache)

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

  private def scenarioMethodOpt(clazz: Class[_]): Option[Method] = findMethods(clazz, classOf[Scenarios]).headOption.map(method => { method.setAccessible(true); method })

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
      allSuiteMembers(testClass) filter
        (findAnnotationRecursively(_, classOf[PartOf]).exists(_.asInstanceOf[PartOf].value() == testClass))
    else
      Nil

  private def allSuiteMembers(testClass: Class[_]): Seq[Class[_]] = {
    val maybeLoadedClasses: Seq[Option[Class[_]]] =
      ClassPath.
        from(getClass.getClassLoader).
        getTopLevelClassesRecursive(testClass.getPackage.getName). // Use a restricting prefix. Otherwise we would load all the classes in the classpath.
        toSeq.
        map(classInfo => Some(classInfo.load))
    val loadedClasses: Seq[Class[_]] = maybeLoadedClasses.flatMap(identity(_))
    val loadedClassesWithInnerClasses = loadedClasses.flatMap(clazz => clazz +: clazz.getDeclaredClasses) // Guava finds only top-level classes, not inner classes.
    loadedClassesWithInnerClasses.filter(findAnnotationRecursively(_, classOf[PartOf]).isDefined)
  }

  private def scenarios(testClass: Class[_]): Option[Seq[AnyRef]] =
    scenarioMethodOpt(testClass) map { scenarioMethod =>
      val scenarios = scenarioMethod.invoke(null).asInstanceOf[java.lang.Iterable[A]]
      scenarios.iterator().toSeq
    }

  private class MethodAndScenario(val scenario: Option[AnyRef], val method: Method)


  private type A = AnyRef
}

private[havarunner] case class TestClassAndSource(testClass: Class[_], testContext: TestContext = DefaultContext)
private[havarunner] trait TestContext
private[havarunner] case class SuiteContext(suiteClass: Class[_]) extends TestContext
private[havarunner] case object DefaultContext extends TestContext