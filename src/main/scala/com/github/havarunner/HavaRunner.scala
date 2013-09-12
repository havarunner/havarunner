package com.github.havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent.{TimeUnit, SynchronousQueue, ThreadPoolExecutor}
import scala.collection.JavaConversions._
import CodingConventionsAndValidations._
import org.junit._
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException
import java.lang.reflect.Method
import org.junit.runners.model.{FrameworkMethod, TestClass}
import java.lang.annotation.Annotation
import org.junit.runner.manipulation.{Filter, Filterable}
import com.github.havarunner.annotation.{Scenarios, RunSequentially}
import com.github.havarunner.HavaRunner._
import com.github.havarunner.exception.TestDidNotRiseExpectedException
import com.github.havarunner.ConcurrencyControl._

class HavaRunner(parentClass: Class[_ <: Any]) extends Runner with Filterable with ThreadPool {

  private var filterOption: Option[Filter] = None

  def getDescription = {
    val description = Description.createSuiteDescription(parentClass)
    children.iterator() foreach (child => description.addChild(describeChild(child)))
    description
  }

  def run(notifier: RunNotifier) {
    children.iterator() foreach (testAndParameters => runChild(testAndParameters, notifier))
    executor shutdown()
    executor awaitTermination(1, TimeUnit.HOURS)
  }

  def filter(filter: Filter) {
    this.filterOption = Some(filter)
  }

  private[havarunner] def runChild(implicit testAndParameters: TestAndParameters, notifier: RunNotifier) {
    val description = describeChild(testAndParameters)
    val testIsInvalidReport = reportInvalidations(testAndParameters)
    if (testIsInvalidReport.isDefined)
      notifier fireTestAssumptionFailed new Failure(description, testIsInvalidReport.get)
    else
      runValidTest(testAndParameters, notifier, description, executor)
  }

  private[havarunner] val classesToTest = parentClass +: parentClass.getDeclaredClasses.toSeq

  private[havarunner] def children: java.lang.Iterable[TestAndParameters] =
    toTestParameters(classesToTest).
      filter(acceptChild(_, filterOption))
}

private object HavaRunner {
  private def acceptChild(testParameters: TestAndParameters, filterOption: Option[Filter]): Boolean =
    filterOption.map(filter => {
      val FilterDescribePattern = "Method (.*)\\((.*)\\)".r
      filter.describe() match {
        case FilterDescribePattern(desiredMethodName, desiredClassName) =>
          val methodNameMatches = testParameters.frameworkMethod.getMethod.getName.equals(desiredMethodName)
          val classNameMatches: Boolean = testParameters.testClass.getJavaClass.getName.equals(desiredClassName)
          classNameMatches && methodNameMatches
        case _ => throw new IllegalArgumentException("Filter#describe returned an unexpected string")
      }
    }).getOrElse(true)

  private def describeChild(testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass.getJavaClass,
      testAndParameters.frameworkMethod.getName + testAndParameters.scenarioToString
      )

  private def runValidTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description, executor: ThreadPoolExecutor) {
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
      withThrottle(testOperation)
    } catch {
      case e: AssumptionViolatedException => eachNotifier addFailedAssumption e
      case e: Throwable => eachNotifier addFailure e
    } finally {
      eachNotifier fireTestFinished()
    }
  }

  private def testOperation(implicit testAndParameters: TestAndParameters): Operation[AnyRef] =
    Operation(() => {
      takingExpectedExceptionIntoAccount {
        testAndParameters.frameworkMethod.invokeExplosively(testAndParameters.testInstance)
      }
    })

  private def takingExpectedExceptionIntoAccount(testF: => A)(implicit testAndParameters: TestAndParameters): A = {
    testAndParameters.expectedException match {
      case Some(expected) =>
        try {
          testF
          throw new TestDidNotRiseExpectedException(expected, testAndParameters)
        } catch {
          case e: Throwable =>
            if (expected != e.getClass) {
              throw e // The exception was something the test did not anticipate
            } else {
              // The test expected this exception. All ok.
              "ok"
            }
        }
      case None =>
        testF
    }
  }

  private def toTestParameters(classesToTest: Seq[Class[_ <: Any]]): Seq[TestAndParameters] = {
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

  type A = AnyRef
}
