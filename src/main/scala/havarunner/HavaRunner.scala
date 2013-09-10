package havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent.{TimeUnit, SynchronousQueue, ThreadPoolExecutor}
import scala.collection.JavaConversions._
import havarunner.HavaRunner._
import havarunner.CodingConventionsAndValidations._
import org.junit._
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException
import java.lang.reflect.Method
import org.junit.runners.model.{FrameworkMethod, TestClass}
import java.lang.annotation.Annotation
import havarunner.annotation.{Scenarios, RunSequentially}

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

  private def runValidTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    if (testAndParameters.frameworkMethod.getAnnotation(classOf[Ignore]) != null) {
      notifier fireTestIgnored description
    } else {
      val testTask = new Runnable {
        def run() {
          try { // TODO Add exception handler to remove nested curly braces
            runLeaf(
              testOperation,
              description,
              notifier
            )
          } catch {
            case e: Throwable => notifier fireTestFailure(new Failure(description, e))
          } finally {
            afters.run
          }
        }
      }
      if (testAndParameters.runSequentially) {
        testTask.run()
      } else {
        executor submit testTask
      }
    }
  }

  private def afters(implicit testAndParameters: TestAndParameters): Operation[Unit] =
    Operation(() =>
      testAndParameters.afters.foreach(invoke(_, testAndParameters))
    )

  private def invoke(method: Method, testAndParameters: TestAndParameters) {
    method.setAccessible(true)
    method.invoke(testAndParameters.testInstance)
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

  private def testOperation(implicit testAndParameters: TestAndParameters): Operation[AnyRef] =
    Operation(() => {
      testAndParameters.frameworkMethod.invokeExplosively(testAndParameters.testInstance)
    })
}

private object HavaRunner {
  private def toTestParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
    classesToTest.flatMap(aClass => {
      val testClass = new TestClass(aClass)
      findTestMethods(testClass).map(methodAndScenario => {
        new TestAndParameters(
          new FrameworkMethod(methodAndScenario.method),
          testClass,
          scenario = methodAndScenario.scenario,
          afters = findMethods(testClass, classOf[After]).reverse /* Reverse, because we want to run the superclass afters AFTER the subclass afters*/,
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

  type A = AnyRef
}
