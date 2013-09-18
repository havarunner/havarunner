package com.github.havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent._
import scala.collection.JavaConversions._
import Validations._
import java.lang.reflect.InvocationTargetException
import org.junit.runner.manipulation.{Filter, Filterable}
import com.github.havarunner.HavaRunner._
import com.github.havarunner.exception.TestDidNotRiseExpectedException
import com.github.havarunner.ConcurrencyControl._
import com.github.havarunner.Parser._
import com.github.havarunner.Reflections._

/**
 * Usage: @org.junit.runner.RunWith(HavaRunner.class)
 *
 * @author Lauri Lehmijoki
 */
class HavaRunner(parentClass: Class[_ <: Any]) extends Runner with Filterable with ThreadPool {

  private var filterOption: Option[Filter] = None // The Filterable API requires us to use a var

  def getDescription = {
    val description = Description.createSuiteDescription(parentClass)
    children.iterator() foreach (child => description.addChild(describeChild(child)))
    description
  }

  def run(notifier: RunNotifier) {
    reportIfSuite
    val afterAllsAndFutures = children.groupBy(_.scenarioAndClass).map {
      case (scenarioAndClass, testsAndClasses) =>
        val futures: Iterable[FutureTask[_]] = testsAndClasses.flatMap(testAndParameters => runChild(testAndParameters, notifier))
        AfterAllsAndFutures(afterAlls(testsAndClasses.head), futures)
    }
    afterAllsAndFutures.foreach(afterAllsAndFutures => {
      afterAllsAndFutures.futures.foreach(_.get(1, TimeUnit.HOURS))
      afterAllsAndFutures.afterAlls.run
    })
    executor shutdown()
    executor awaitTermination(1, TimeUnit.HOURS)
  }

  def filter(filter: Filter) {
    this.filterOption = Some(filter)
  }

  private[havarunner] def reportIfSuite = {
    children.filter(_.testContext.isInstanceOf[SuiteContext]).foreach(testAndParameters => {
      val suiteContext: SuiteContext = testAndParameters.testContext.asInstanceOf[SuiteContext]
      println(s"[HavaRunner] Running ${testAndParameters.testClass.getName} as a part of ${suiteContext.suiteClass.getName}")
    })
  }

  private[havarunner] def runChild(implicit testAndParameters: TestAndParameters, notifier: RunNotifier): Option[FutureTask[_]] = {
    implicit val description = describeChild
    val testIsInvalidReport = reportInvalidations
    if (testIsInvalidReport.isDefined) {
      notifier fireTestFailure  new Failure(description, testIsInvalidReport.get)
      None
    } else {
      runValidTest
    }
  }

  private[havarunner] val classesToTest = withSubclasses(parentClass)

  private[havarunner] lazy val children: java.lang.Iterable[TestAndParameters ] =
    parseTestsAndParameters(classesToTest).filter(acceptChild(_, filterOption))

  private case class AfterAllsAndFutures(afterAlls: Operation[_], futures: Iterable[FutureTask[_]])
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

  private def describeChild(implicit testAndParameters: TestAndParameters) =
    Description createTestDescription(
      testAndParameters.testClass,
      testAndParameters.testMethod.getName + testAndParameters.scenarioToString
      )

  private def runValidTest(implicit testAndParameters: TestAndParameters , notifier: RunNotifier, description: Description, executor: ForkJoinPool): Option[FutureTask[_]] =
    if (testAndParameters.ignored) {
      notifier fireTestIgnored description
      None
    } else {
      val testTask = new FutureTask(new Runnable {
        def run() {
          try {
            notifier fireTestStarted description
            withThrottle(testOperation)
          } catch {
            case e: Throwable => notifier fireTestFailure new Failure(description, e)
          } finally {
            notifier fireTestFinished description
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

  private def afterAlls(implicit testAndParameters: TestAndParameters ): Operation[Unit] =
    Operation(() =>
      testAndParameters.afterAll.foreach(invoke(_, testAndParameters))
    )

  private def testOperation(implicit testAndParameters: TestAndParameters ): Operation[AnyRef] =
    Operation(() => {
      takingExpectedExceptionIntoAccount {
        try {
          testAndParameters.testMethod.invoke(TestInstanceCache.fromTestInstanceCache)
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
