package com.github.havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent._
import scala.collection.JavaConversions._
import CodingConventionsAndValidations._
import org.junit._
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException
import java.lang.reflect.{InvocationTargetException, Method}
import org.junit.runner.manipulation.{Filter, Filterable}
import com.github.havarunner.HavaRunner._
import com.github.havarunner.exception.TestDidNotRiseExpectedException
import com.github.havarunner.ConcurrencyControl._
import com.github.havarunner.Parser._
import scala.Some

class HavaRunner(parentClass: Class[_ <: Any]) extends Runner with Filterable with ThreadPool {

  private var filterOption: Option[Filter] = None // The Filterable API requires us to use a var

  def getDescription = {
    val description = Description.createSuiteDescription(parentClass)
    children.iterator() foreach (child => description.addChild(describeChild(child)))
    description
  }

  def run(notifier: RunNotifier) {
    children.groupBy(_.scenarioAndClass).foreach {
      case (scenarioAndClass, testsAndClasses) =>
        val forkJoinTasks = testsAndClasses flatMap (testAndParameters => runChild(testAndParameters, notifier))
        forkJoinTasks.foreach(_.get(1, TimeUnit.HOURS))
        testsAndClasses.headOption.foreach(afterAlls(_).run)
    }
    executor shutdown()
    executor awaitTermination(1, TimeUnit.HOURS)
  }

  def filter(filter: Filter) {
    this.filterOption = Some(filter)
  }

  private[havarunner] def runChild(implicit testAndParameters: TestAndParameters, notifier: RunNotifier): Option[FutureTask[_]] = {
    val description = describeChild(testAndParameters)
    val testIsInvalidReport = reportInvalidations(testAndParameters)
    if (testIsInvalidReport.isDefined) {
      notifier fireTestFailure  new Failure(description, testIsInvalidReport.get)
      None
    } else {
      runValidTest(testAndParameters, notifier, description, executor)
    }
  }

  private[havarunner] val classesToTest = parentClass +: parentClass.getDeclaredClasses.toSeq

  private[havarunner] def children: java.lang.Iterable[TestAndParameters] =
    parseTestsAndParameters(classesToTest).
      filter(acceptChild(_, filterOption))
}

private object HavaRunner {
  private def acceptChild(testParameters: TestAndParameters, filterOption: Option[Filter]): Boolean =
    filterOption.map(filter => {
      val FilterDescribePattern = "Method (.*)\\((.*)\\)".r
      filter.describe() match {
        case FilterDescribePattern(desiredMethodName, desiredClassName) =>
          val methodNameMatches = testParameters.testMethod.getName.equals(desiredMethodName)
          val classNameMatches: Boolean = testParameters.testClass.getName.equals(desiredClassName)
          classNameMatches && methodNameMatches
        case unexpected => throw new IllegalArgumentException(s"Filter#describe returned an unexpected string $unexpected")
      }
    }).getOrElse(true)

  private def describeChild(testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass,
      testAndParameters.testMethod.getName + testAndParameters.scenarioToString
      )

  private def runValidTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description, executor: ForkJoinPool): Option[FutureTask[_]] = {
    if (testAndParameters.testMethod.getAnnotation(classOf[Ignore]) != null) {
      notifier fireTestIgnored description
      None
    } else {
      val testTask = new FutureTask(new Runnable {
        def run() {
          try { // TODO Add exception handler to remove nested curly braces
            withStartAndFinished(
              testOperation,
              description,
              notifier
            )
          } catch {
            case e: Throwable => notifier fireTestFailure(new Failure(description, e))
          }
        }
      }, None)
      if (testAndParameters.runSequentially) {
        testTask.run()
      } else {
        val forkJoinTask = ForkJoinTask.adapt(testTask)
        executor submit forkJoinTask
      }
      Some(testTask)
    }
  }

  private def afterAlls(implicit testAndParameters: TestAndParameters): Operation[Unit] =
    Operation(() =>
      testAndParameters.afterAll.foreach(invoke(_, testAndParameters))
    )

  private def invoke(method: Method, testAndParameters: TestAndParameters) {
    method.setAccessible(true)
    method.invoke(testAndParameters.testInstance)
  }

  private def withStartAndFinished(testOperation: Operation[_ <: Any], description: Description, notifier: RunNotifier) {
    val eachNotifier = new EachTestNotifier(notifier, description)
    eachNotifier fireTestStarted()
    try {
      withThrottle(testOperation)
    } catch {
      case e: Throwable => eachNotifier addFailure e
    } finally {
      eachNotifier fireTestFinished()
    }
  }

  private def testOperation(implicit testAndParameters: TestAndParameters): Operation[AnyRef] =
    Operation(() => {
      takingExpectedExceptionIntoAccount {
        try {
          testAndParameters.testMethod.invoke(testAndParameters.testInstance)
        } catch {
          case e: InvocationTargetException =>
            throw e.getTargetException
        }
      }
    })

  private def takingExpectedExceptionIntoAccount(testF: => AnyRef)(implicit testAndParameters: TestAndParameters): AnyRef = {
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
}
