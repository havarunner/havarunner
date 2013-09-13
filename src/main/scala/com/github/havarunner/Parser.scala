package com.github.havarunner

import org.junit.runners.model.{FrameworkMethod, TestClass}
import org.junit.{Test, After}
import com.github.havarunner.annotation.{Scenarios, RunSequentially}
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import scala.collection.JavaConversions._

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] =
    classesToTest.flatMap(aClass => {
      val testClass = new TestClass(aClass)
      findTestMethods(testClass).map(methodAndScenario => {
        new TestAndParameters(
          new FrameworkMethod(methodAndScenario.method),
          testClass,
          expectedException = expectedException(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          afters = findMethods(testClass, classOf[After]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          runSequentially = classesToTest.exists(isAnnotatedWith(_, classOf[RunSequentially]))
        )
      })
    })

  private def isAnnotatedWith(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Boolean = {
    if (clazz.getAnnotation(annotationClass) != null) {
      true
    } else if (clazz.getSuperclass != null) {
      isAnnotatedWith(clazz.getSuperclass, annotationClass)
    } else {
      false
    }
  }

  private def expectedException(method: Method): Option[Class[_ <: Throwable]] = {
    val expected = method.getAnnotation(classOf[Test]).expected()
    if (expected == classOf[org.junit.Test.None])
      None
    else
      Some(expected)
  }

  private def findMethods(testClass: TestClass, annotation: Class[_ <: Annotation]): Seq[Method] = findMethods(testClass.getJavaClass, annotation)

  private def findMethods(clazz: Class[_], annotation: Class[_ <: Annotation]): Seq[Method] = {
    val superclasses: Seq[Class[_ <: Any]] = classWithSuperclasses(clazz)
    superclasses.flatMap(clazz =>
      clazz.getDeclaredMethods.filter(_.getAnnotation(annotation) != null)
    )
  }


  private def classWithSuperclasses(clazz: Class[_ <: Any], superclasses: Seq[Class[_ <: Any]] = Nil): Seq[Class[_ <: Any]] = {
    if (clazz.getSuperclass != null) {
      classWithSuperclasses(clazz.getSuperclass, clazz +: superclasses)
    } else {
      superclasses
    }
  }

  private def isScenarioClass(clazz: Class[_]) = scenarioMethod(clazz).isDefined

  private def scenarioMethod(clazz: Class[_]): Option[Method] = findMethods(clazz, classOf[Scenarios]).headOption.map(method => { method.setAccessible(true); method })

  private def findTestMethods(testClass: TestClass): Seq[MethodAndScenario] = {
    val testMethods = testClass.getJavaClass.getDeclaredMethods.
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

  private def scenarios(testClass: TestClass): Option[Seq[AnyRef]] = {
    if (isScenarioClass(testClass.getJavaClass)) {
      val scenarios = scenarioMethod(testClass.getJavaClass).get.invoke(null).asInstanceOf[java.lang.Iterable[A]]
      Some(scenarios.iterator().toSeq)
    } else {
      None
    }
  }

  private class MethodAndScenario(val scenario: Option[AnyRef], val method: Method)

  private type A = AnyRef
}
