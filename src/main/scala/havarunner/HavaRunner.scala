package havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent.{TimeUnit, SynchronousQueue, ThreadPoolExecutor}
import scala.collection.JavaConversions._
import havarunner.HavaRunner._
import havarunner.CodingConventionsAndValidations._
import havarunner.ScenarioHelper._
import org.junit._
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException
import java.lang.reflect.Method
import org.junit.runners.model.{FrameworkMethod, TestClass}
import java.lang.annotation.Annotation
import scala.Some
import havarunner.annotation.RunSequentially

class HavaRunner(parentClass: Class[_ <: Any]) extends Runner {
  val executor = new ThreadPoolExecutor(
    0, Runtime.getRuntime.availableProcessors(),
    60L, TimeUnit.SECONDS,
    new SynchronousQueue[Runnable],
    new ThreadPoolExecutor.CallerRunsPolicy()
  )

  def getDescription = {
    val description = Description.createSuiteDescription(parentClass)
    getChildren.iterator() foreach (child => description.addChild(describeChild(child)))
    description
  }

  def run(notifier: RunNotifier) {
    getChildren.iterator() foreach (testAndParameters => runChild(testAndParameters, notifier))
    executor shutdown()
    executor awaitTermination(1, TimeUnit.HOURS)
  }

  private[havarunner] val classesToTest = parentClass +: parentClass.getDeclaredClasses.toSeq

  private[havarunner] def getChildren: java.lang.Iterable[TestAndParameters] = toTestParameters(classesToTest)

  private[havarunner] def runChild(testAndParameters: TestAndParameters, notifier: RunNotifier) {
    val description = describeChild(testAndParameters)
    val testIsInvalidReport = reportInvalidations(testAndParameters)
    if (testIsInvalidReport.isDefined)
      notifier fireTestAssumptionFailed new Failure(description, testIsInvalidReport.get)
    else
      runValidTest(testAndParameters, notifier, description)
  }

  private def describeChild(testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass.getJavaClass,
      testAndParameters.frameworkMethod.getName + testAndParameters.scenarioToString
      )

  private def runValidTest(testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    if (testAndParameters.frameworkMethod.getAnnotation(classOf[Ignore]) != null) {
      notifier fireTestIgnored description
    } else {
      val testClassInstance = newTestClassInstance(testAndParameters.testClass)
      val testOperation =
        runBeforeClasses(testAndParameters) andThen
          (runBefores(testAndParameters, testClassInstance) andThen
            runTest(testAndParameters, testClassInstance)) andThen
              runAfterClasses(testAndParameters)
      val testTask = new Runnable {
        def run() {
          runLeaf(
            testOperation,
            description,
            notifier
          )
        }
      }
      if (testAndParameters.runSequentially) {
        testTask.run()
      } else {
        executor submit testTask
      }
    }
  }

  private def runBeforeClasses(testAndParameters: TestAndParameters): Operation[Unit] =
    Operation(() =>
      testAndParameters.beforeClasses.foreach(invoke(_))
    )

  private def runBefores(testAndParameters: TestAndParameters, testClassInstance: AnyRef): Operation[Unit] =
    Operation(() =>
      testAndParameters.befores.foreach(invoke(_, Some(testClassInstance)))
    )

  private def runAfterClasses(testAndParameters: TestAndParameters): Operation[Unit] =
    Operation(() =>
      testAndParameters.afterClasses.foreach(invoke(_))
    )

  private def invoke(method: Method, thisObject: Option[AnyRef] = None) {
    method.setAccessible(true)
    method.invoke(thisObject.getOrElse(null))
  }

  private def runLeaf(testOperation: Operation[_ <: Any], description: Description, notifier: RunNotifier) {
    val eachNotifier = new EachTestNotifier(notifier, description)
    eachNotifier fireTestStarted()
    try {
      testOperation.run
    } catch {
      case e: AssumptionViolatedException => eachNotifier addFailedAssumption e
      case e: Throwable => eachNotifier addFailure e
    } finally {
      eachNotifier fireTestFinished()
    }
  }

  private def runTest(testAndParameters: TestAndParameters, testClassInstance: AnyRef): Operation[AnyRef] = {
    if (isScenarioClass(testAndParameters.testClass.getJavaClass))
      createScenarioTestFunction(testAndParameters, testClassInstance)
    else
      Operation(() => {
        testAndParameters.frameworkMethod.invokeExplosively(testClassInstance)
      })
  }
}

private object HavaRunner {
  private def toTestParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    classesToTest.flatMap(aClass => {
      val testClass = new TestClass(aClass)
      findTestMethods(testClass).map(methodAndScenario => {
        new TestAndParameters(
          new FrameworkMethod(methodAndScenario.method),
          testClass,
          scenario = methodAndScenario.scenario.asInstanceOf[Object],
          beforeClasses = findMethods(testClass, classOf[BeforeClass]),
          befores = findMethods(testClass, classOf[Before]),
          afterClasses = findMethods(testClass, classOf[AfterClass]),
          runSequentially = classesToTest.exists(isAnnotatedWith(_, classOf[RunSequentially]))
        )
      })
    })
  }

  private def isAnnotatedWith(clazz: Class[_ <: Any], annotationClass: Class[_ <: Annotation]): Boolean = {
    if (clazz.getAnnotation(annotationClass) != null) {
      true
    } else if (clazz.getSuperclass != null) {
      isAnnotatedWith(clazz.getSuperclass, annotationClass)
    } else {
      false
    }
  }

  private def findMethods(testClass: TestClass, annotation: Class[_ <: Annotation]) = {
    val superclasses: Seq[Class[_ <: Any]] = classWithSuperclasses(testClass.getJavaClass)
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

  private def findOnlyConstructor(testClass: TestClass) = {
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

  private def isScenarioClass(clazz: Class[_ <: Any]) = classOf[TestWithMultipleScenarios[A]].isAssignableFrom(clazz)

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

  private def newTestClassInstance(testClass: TestClass) = findOnlyConstructor(testClass).newInstance().asInstanceOf[AnyRef]

  private class MethodAndScenario(val scenario: Any, val method: Method)

  type A = Any
}
