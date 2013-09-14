package com.github.havarunner

import org.junit.{Test, After}
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import java.lang.reflect.Method
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath
import java.util.logging.{Level, Logger}

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    val localAndSuiteClasses = classesToTest ++ classesToTest.flatMap(findClassesIfSuite)
    localAndSuiteClasses.flatMap(testClass => {
      findTestMethods(testClass).map(methodAndScenario => {
        new TestAndParameters(
          methodAndScenario.method,
          testClass,
          expectedException = expectedException(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          afterAll = findMethods(testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          runSequentially = classesToTest.exists(isAnnotatedWith(_, classOf[RunSequentially]))
        )
      })
    })
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
    val testMethods = testClass.getDeclaredMethods.
      filter(_.getAnnotation(classOf[Test]) != null).
      map(method => { method.setAccessible(true); method })
    scenarios(testClass) match {
      case Some(scenarios) =>
        scenarios.flatMap(scenario =>
          testMethods.map(new MethodAndScenario(Some(scenario), _))
        )
      case None =>
        testMethods.map(new MethodAndScenario(None, _))
    }
  }

  private def findClassesIfSuite(testClass: Class[_]): Seq[Class[_]] =
    if (classOf[HavaRunnerSuite[_]].isAssignableFrom(testClass))
      suiteMembers.filter(_.getAnnotation(classOf[PartOf]).value() == testClass)
    else
      Nil

  private lazy val suiteMembers: Seq[Class[_]] = {
    print("HavaRunner is scanning for suite members (this takes a while) ...")
    val maybeLoadedClasses: Seq[Option[Class[_]]] =
      ClassPath.
        from(getClass.getClassLoader).
        getTopLevelClasses.
        toSeq.
        filterNot(_.getPackageName.matches("^(scala|com.sun|sun|java|javax).*")).
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
