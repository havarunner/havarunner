package com.github.havarunner

import org.junit._
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import com.github.havarunner.SuiteCache._
import java.lang.reflect.{Modifier, Method}
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath
import scala.Some

private[havarunner] object Parser {

  def parseTestsAndParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] =
    localAndSuiteTests(classesToTest).flatMap(implicit testClassAndSource =>
      findTestMethods(testClassAndSource.testClass).map(implicit methodAndScenario =>
        TestAndParameters(
          testMethod = methodAndScenario.method,
          testClass = testClassAndSource.testClass,
          rules = findFields(testClassAndSource.testClass, classOf[Rule]).map(f => { f.setAccessible(true); f }),
          ignored = isIgnored,
          expectedException = expectedException(methodAndScenario.method),
          timeout = timeout(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          partOf = suiteOption,
          testContext = testClassAndSource.testContext,
          afterAll = findMethods(testClassAndSource.testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          after = findMethods(testClassAndSource.testClass, classOf[After]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
          before = findMethods(testClassAndSource.testClass, classOf[Before]),
          runSequentially = runSequentially(Some(testClassAndSource.testClass)) orElse runSequentially(suiteOption)
        )
      )
    )

  private def isIgnored(implicit methodAndScenario: MethodAndScenario, testClassAndSource: TestClassAndSource) = {
    val methodIgnored = methodAndScenario.method.getAnnotation(classOf[Ignore]) != null
    val classIgnored = findAnnotationRecursively(testClassAndSource.testClass, classOf[Ignore]).isDefined
    val enclosingClassIgnored =
      if (testClassAndSource.testClass.getEnclosingClass != null)
        findAnnotationRecursively(testClassAndSource.testClass.getEnclosingClass, classOf[Ignore]).isDefined
      else
        false
    methodIgnored || classIgnored || enclosingClassIgnored
  }

  private def runSequentially(maybeClass: Option[Class[_]]): Option[RunSequentially] =
    maybeClass flatMap { clazz =>
      Option(clazz.getAnnotation(classOf[RunSequentially])) orElse
        runSequentially(Option(clazz.getSuperclass)) orElse
        runSequentially(Option(clazz.getDeclaringClass))
    }


  private def suiteOption(implicit testClassAndSource: TestClassAndSource): Option[Class[_ <:HavaRunnerSuite[_]]] =
    findAnnotationRecursively(testClassAndSource.testClass, classOf[PartOf]).
      map(_.asInstanceOf[PartOf]).
      map(_.value())

  private def localAndSuiteTests(classesToTest: Seq[Class[_ <: Any]]): Seq[TestClassAndSource] = {
    val nonSuiteTests = classesToTest.map(TestClassAndSource(_))
    val suiteTests = classesToTest.flatMap(classToTest =>
      findSuiteMembers(classToTest).map(TestClassAndSource(_, SuiteContext(classToTest)))
    )
    (nonSuiteTests ++ suiteTests).filterNot(testClassAndSource => Modifier.isAbstract(testClassAndSource.testClass.getModifiers))
  }.distinct

  private def expectedException(method: Method): Option[Class[_ <: Throwable]] = {
    val expected = method.getAnnotation(classOf[Test]).expected()
    if (expected == classOf[org.junit.Test.None])
      None
    else
      Some(expected)
  }
  
  private def timeout(method: Method): Option[Long] = {
    val timeoutInMillis = method.getAnnotation(classOf[Test]).timeout()
    if (timeoutInMillis == 0)
      None
    else
      Some(timeoutInMillis)
  }

  private def scenarioMethodOpt(clazz: Class[_]): Option[Method] =
    findMethods(clazz, classOf[Scenarios]).
      headOption.
      map(method => { method.setAccessible(true); method })

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

  private def findSuiteMembers(maybeSuiteClass: Class[_]): Seq[Class[_]] =
    if (classOf[HavaRunnerSuite[_]].isAssignableFrom(maybeSuiteClass)) {
      val explicitSuiteMembers = allSuiteMembers(maybeSuiteClass) filter isExplicitSuiteMember(maybeSuiteClass.asInstanceOf[Class[HavaRunnerSuite[_]]])
      explicitSuiteMembers ++ implicitSuiteMembers(explicitSuiteMembers)
    } else
      Nil

  private def isExplicitSuiteMember(suiteClass: Class[HavaRunnerSuite[_]])(clazz: Class[_]): Boolean =
    findAnnotationRecursively(clazz, classOf[PartOf]).exists(_.asInstanceOf[PartOf].value() == suiteClass)

  private def implicitSuiteMembers(explicitSuiteMembers: Seq[Class[_]]): Seq[Class[_]] =
    explicitSuiteMembers.flatMap(clazz => findDeclaredClasses(clazz))

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
      val scenarios = scenarioMethod.invoke(null).asInstanceOf[java.lang.Iterable[AnyRef]]
      scenarios.iterator().toSeq
    }

  private class MethodAndScenario(val scenario: Option[AnyRef], val method: Method)
  private[havarunner] case class TestClassAndSource(testClass: Class[_], testContext: TestContext = DefaultContext)
}