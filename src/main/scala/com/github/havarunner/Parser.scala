package com.github.havarunner

import org.junit.{Ignore, Test}
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import com.github.havarunner.SuiteCache._
import java.lang.reflect.{Modifier, Method}
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[ClassAndOuter]): Seq[TestAndParameters ] = {
    val tests: Seq[TestAndParameters] = localAndSuiteTests(classesToTest).flatMap((testClassAndSource: TestClassAndSource) => {
      findTestMethods(testClassAndSource.classAndOuter).map(methodAndScenario => {
        new TestAndParameters(
          testMethod = methodAndScenario.method,
          testClass = testClassAndSource.classAndOuter.clazz,
          outerClass = testClassAndSource.classAndOuter.outer,
          outerTest = None,
          ignored = methodAndScenario.method.getAnnotation(classOf[Ignore]) != null || isAnnotatedWith(testClassAndSource.classAndOuter.clazz, classOf[Ignore]),
          expectedException = expectedException(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          partOf = suiteOption(testClassAndSource.classAndOuter.clazz),
          testContext = testClassAndSource.testContext,
          afterAll = findMethods(testClassAndSource.classAndOuter.clazz, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          runSequentially = classesToTest.exists(classAndEnclosed => isAnnotatedWith(classAndEnclosed.clazz, classOf[RunSequentially]))
        )
      })
    })
    tests.map(test => {
      val outer: Option[TestAndParameters] =
        if (!Modifier.isStatic(test.testClass.getModifiers))
          test.outerClass.flatMap(clazz => tests.find(_.testClass == clazz))
        else
          None
      test.copy(outerTest = outer)
    })
  }

  private def suiteOption(implicit clazz: Class[_]): Option[HavaRunnerSuite[_]] =
    Option(clazz.getAnnotation(classOf[PartOf])) map {
      partOfAnnotation =>
        val suiteClass = partOfAnnotation.value()
        fromSuiteInstanceCache(suiteClass)
    }

  private def localAndSuiteTests(classesToTest: Seq[ClassAndOuter]): Seq[TestClassAndSource] = {
    val nonSuiteTests = classesToTest.map(TestClassAndSource(_))
    val suiteTests = classesToTest.flatMap(classToTest =>
      findSuiteMembers(classToTest.clazz).map((suiteMember: ClassAndOuter) => TestClassAndSource(suiteMember, SuiteContext(classToTest.clazz)))
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

  private def findTestMethods(testClass: ClassAndOuter): Seq[MethodAndScenario] = {
    val testMethods = findMethods(testClass.clazz, classOf[Test]).map(method => { method.setAccessible(true); method })
    scenarios(testClass.clazz) match {
      case Some(scenarios) =>
        scenarios.flatMap(scenario =>
          testMethods.map(MethodAndScenario(Some(scenario), _))
        )
      case None =>
        testMethods.map(MethodAndScenario(None, _))
    }
  }

  private def findSuiteMembers(testClass: Class[_]): Seq[ClassAndOuter] =
    if (classOf[HavaRunnerSuite[_]].isAssignableFrom(testClass))
      suiteMembers(testClass).
        filter(_.getAnnotation(classOf[PartOf]).value() == testClass).
        map(clazz => ClassAndOuter(clazz, Option(clazz.getDeclaringClass)))
    else
      Nil

  private def suiteMembers(testClass: Class[_]): Seq[Class[_]] = {
    val maybeLoadedClasses: Seq[Option[Class[_]]] =
      ClassPath.
        from(getClass.getClassLoader).
        getTopLevelClassesRecursive(testClass.getPackage.getName). // Use a restricting prefix. Otherwise we would load all the classes in the classpath.
        toSeq.
        map(classInfo => Some(classInfo.load))
    val loadedClasses: Seq[Class[_]] = maybeLoadedClasses.flatMap(identity(_))
    loadedClasses.filter(_.isAnnotationPresent(classOf[PartOf]))
  }


  private def scenarios(testClass: Class[_]): Option[Seq[AnyRef]] =
    scenarioMethodOpt(testClass) map { scenarioMethod =>
      val scenarios = scenarioMethod.invoke(null).asInstanceOf[java.lang.Iterable[A]]
      scenarios.iterator().toSeq
    }

  private case class MethodAndScenario(scenario: Option[AnyRef], method: Method)

  private type A = AnyRef
}

private[havarunner] case class ClassAndOuter(clazz: Class[_], outer: Option[Class[_]])
private[havarunner] case class TestClassAndSource(classAndOuter: ClassAndOuter, testContext: TestContext = DefaultContext)
private[havarunner] trait TestContext
private[havarunner] case class SuiteContext(suiteClass: Class[_]) extends TestContext
private[havarunner] case object DefaultContext extends TestContext