package com.github.havarunner

import org.junit._
import com.github.havarunner.annotation.{AfterAll, PartOf, RunSequentially, Scenarios}
import java.lang.reflect.Method
import java.lang.reflect.Modifier._
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath
import org.junit.runners.model.FrameworkMethod

/**
 * Place here code that is related to discovering tests and their parameters from the source classes.
 */
private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_]]): Seq[TestAndParameters] =
    localAndSuiteTests(classesToTest).flatMap(implicit testClass =>
      findTestMethods(testClass).map(implicit methodAndScenario =>
        TestAndParameters(
          testMethod = new FrameworkMethod(methodAndScenario.method),
          testClass = testClass,
          rules = findFields(testClass, classOf[Rule]),
          ignored = isIgnored,
          expectedException = expectedException(methodAndScenario.method),
          timeout = timeout(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          partOf = suiteOption,
          afterAll = findMethods(testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          after = findMethods(testClass, classOf[After]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          before = findMethods(testClass, classOf[Before]),
          runSequentially = runSequentially(Some(testClass)) orElse runSequentially(suiteOption)
        )
      )
    )

  def isIgnored(implicit methodAndScenario: MethodAndScenario, testClass: Class[_]) = {
    val methodIgnored = methodAndScenario.method.getAnnotation(classOf[Ignore]) != null
    val classIgnored = findAnnotationRecursively(testClass, classOf[Ignore]).isDefined
    val enclosingClassIgnored =
      if (testClass.getEnclosingClass != null)
        findAnnotationRecursively(testClass.getEnclosingClass, classOf[Ignore]).isDefined
      else
        false
    methodIgnored || classIgnored || enclosingClassIgnored
  }

  def runSequentially(maybeClass: Option[Class[_]]): Option[RunSequentially] =
    maybeClass flatMap { clazz =>
      Option(clazz.getAnnotation(classOf[RunSequentially])) orElse
        runSequentially(Option(clazz.getSuperclass)) orElse
        runSequentially(Option(clazz.getDeclaringClass))
    }


  def suiteOption(implicit testClass: Class[_]): Option[Class[_ <:HavaRunnerSuite[_]]] =
    findAnnotationRecursively(testClass, classOf[PartOf]).
      map(_.asInstanceOf[PartOf]).
      map(_.value())

  def localAndSuiteTests(classesToTest: Seq[Class[_]]): Seq[Class[_]] = {
    val nonSuiteTests = classesToTest
    val suiteTests = classesToTest.flatMap(classToTest =>
      findSuiteMembers(classToTest)
    )
    (nonSuiteTests ++ suiteTests).filterNot(testClass => isAbstract(testClass.getModifiers))
  }.distinct

  def expectedException(method: Method): Option[Class[_ <: Throwable]] = {
    val expected = method.getAnnotation(classOf[Test]).expected()
    if (expected == classOf[org.junit.Test.None])
      None
    else
      Some(expected)
  }
  
  def timeout(method: Method): Option[Long] = {
    val timeoutInMillis = method.getAnnotation(classOf[Test]).timeout()
    if (timeoutInMillis == 0)
      None
    else
      Some(timeoutInMillis)
  }

  def scenarioMethodOpt(clazz: Class[_]): Option[Method] =
    findMethods(clazz, classOf[Scenarios]).
      headOption

  def findTestMethods(testClass: Class[_]): Seq[MethodAndScenario] = {
    val testMethods = findMethods(testClass, classOf[Test])
    scenarios(testClass) match {
      case Some(scenarios) =>
        scenarios.flatMap(scenario =>
          testMethods.map(new MethodAndScenario(Some(scenario), _))
        )
      case None =>
        testMethods.map(new MethodAndScenario(None, _))
    }
  }

  def findSuiteMembers(maybeSuiteClass: Class[_]): Seq[Class[_]] =
    if (classOf[HavaRunnerSuite[_]].isAssignableFrom(maybeSuiteClass)) {
      val explicitSuiteMembers = allSuiteMembers(maybeSuiteClass) filter isExplicitSuiteMember(maybeSuiteClass.asInstanceOf[Class[HavaRunnerSuite[_]]])
      explicitSuiteMembers ++ implicitSuiteMembers(explicitSuiteMembers)
    } else
      Nil

  def isExplicitSuiteMember(suiteClass: Class[HavaRunnerSuite[_]])(clazz: Class[_]): Boolean =
    findAnnotationRecursively(clazz, classOf[PartOf]).exists(_.asInstanceOf[PartOf].value() == suiteClass)

  def implicitSuiteMembers(explicitSuiteMembers: Seq[Class[_]]): Seq[Class[_]] =
    explicitSuiteMembers.flatMap(clazz => findDeclaredClasses(clazz))

  def allSuiteMembers(testClass: Class[_]): Seq[Class[_]] = {
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

  def scenarios(testClass: Class[_]): Option[Seq[AnyRef]] =
    scenarioMethodOpt(testClass) map { scenarioMethod =>
      val scenarios = ensureAccessible(scenarioMethod).invoke(null).asInstanceOf[java.lang.Iterable[AnyRef]]
      scenarios.iterator().toSeq
    }

  class MethodAndScenario(val scenario: Option[AnyRef], val method: Method)
}