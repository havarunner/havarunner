package com.github.havarunner

import org.junit._
import com.github.havarunner.annotation.{PartOf, AfterAll, Scenarios, RunSequentially}
import java.lang.reflect.{Modifier, Method}
import java.lang.reflect.Modifier._
import scala.collection.JavaConversions._
import com.github.havarunner.Reflections._
import com.google.common.reflect.ClassPath

/**
 * Place here code that is related to discovering tests and their parameters from the source classes.
 */
private[havarunner] object Parser {

  type ParseResult = Seq[TestAndParameters]

  def parseTestsAndParameters(classesToTest: Seq[Class[_]]): ParseResult = {
    val parseResult = localAndSuiteTests(classesToTest).flatMap(implicit testClass =>
      findTestMethods(testClass).map(implicit methodAndScenario => {
        val withoutSequentialityProperties = TestAndParameters(
          testMethod = methodAndScenario.method,
          testClass = testClass,
          rules = findFields(testClass, classOf[Rule]),
          ignored = isIgnored,
          expectedException = expectedException(methodAndScenario.method),
          timeout = timeout(methodAndScenario.method),
          scenario = methodAndScenario.scenario,
          partOf = suiteOption(testClass),
          afterAll = findMethods(testClass, classOf[AfterAll]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/
        )
        withoutSequentialityProperties.copy(
          before = findMethods(withoutSequentialityProperties.testClass, classOf[Before]).map(Pair(_, withoutSequentialityProperties)),
          after = findMethods(withoutSequentialityProperties.testClass, classOf[After]).map(Pair(_, withoutSequentialityProperties)).reverse, /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/
          runSequentially = runSequentially(Some(withoutSequentialityProperties.testClass)) orElse runSequentially(suiteOption(withoutSequentialityProperties.testClass))
        )
      })
    )

    parseResult flatMap desugarNonStaticInnerClasses(parseResult)
  }

  def desugarNonStaticInnerClasses(parseResult: ParseResult)(testAndParameters: TestAndParameters): ParseResult =
    enclosingClassForNonStaticTestClass(testAndParameters)
      .map(generateTestsFromNonStaticInnerClasses(parseResult, testAndParameters))
      .getOrElse(testAndParameters :: Nil)

  def generateTestsFromNonStaticInnerClasses(parseResults: ParseResult, testAndParameters: TestAndParameters)(enclosingClass: Class[_]): Seq[TestAndParameters] = {
    require(Option(testAndParameters.testClass.getEnclosingClass).exists(_ == enclosingClass), "This function is defined only for nested tests")
    def topmostEncloser(nestedClass: Class[_])(enclosingCandidate: TestAndParameters) = {
      val enclosesOurNestedClass = nestedClass.getEnclosingClass == enclosingCandidate.testClass
      val isStaticOrTopLevel = enclosingCandidate.testClass.getEnclosingClass == null || isStatic(enclosingCandidate.testClass.getModifiers)
      enclosesOurNestedClass && isStaticOrTopLevel
    }
    def closestWithoutEnclosingClass(maybeClass: Option[Class[_]]): Option[TestAndParameters] =
      maybeClass.flatMap(clazz =>
        parseResults
          .find(topmostEncloser(clazz))
          .orElse(closestWithoutEnclosingClass(Option(clazz.getEnclosingClass)))
      )

    val rootEncloser = closestWithoutEnclosingClass(Option(testAndParameters.testClass))
      .get // The option should always be defined, because we are dealing with non-static inner classes in this function

    val scenarios: Seq[AnyRef] = parseResults
      .filter(_.testClass == rootEncloser.testClass)
      .flatMap(_.scenario)
      .distinct // Here we count on proper Object#equals implementation of the scenario class
    
    def applyParamsToInnerNonStatic(testAndParams: TestAndParameters, scenarioOpt: Option[AnyRef]) =
      testAndParameters.copy(
        encloser = Some(ScenarioAndClass(enclosingClass, scenarioOpt)),
        partOf = rootEncloser.partOf,
        scenario = scenarioOpt
        //before = ???,
        //after = ??? // TODO
      )

    if (scenarios.isEmpty)
      applyParamsToInnerNonStatic(testAndParameters, scenarioOpt = None) :: Nil
    else
      scenarios.map(scenarioObject =>
        applyParamsToInnerNonStatic(testAndParameters, scenarioOpt = Some(scenarioObject))
      )
  }

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


  def suiteOption(testClass: Class[_]): Option[Class[_ <:HavaRunnerSuite[_]]] =
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