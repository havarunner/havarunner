package com.github.havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent._
import scala.collection.JavaConversions._
import Validations._
import java.lang.reflect.{Field, InvocationTargetException}
import org.junit.runner.manipulation.{Filter, Filterable}
import com.github.havarunner.HavaRunner._
import com.github.havarunner.exception.{TestDidNotRiseExpectedException}
import com.github.havarunner.ConcurrencyControl._
import com.github.havarunner.Parser._
import com.github.havarunner.Reflections._
import org.junit.internal.AssumptionViolatedException
import com.github.havarunner.TestInstanceCache._
import org.junit.runners.model.Statement
import org.junit.rules.TestRule

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
    children
      .groupBy(_.scenarioAndClass)
      .map {
        case (scenarioAndClass, testsAndClasses) =>
          val futures: Iterable[FutureTask[_]] = testsAndClasses.flatMap(testAndParameters => runChild(testAndParameters, notifier))
          AfterAllsAndFutures(
            afterAlls(testsAndClasses.head), // head is enough, since all the tests share the same instance, because we've grouped by #scenarioAndClass
            futures
          )
      }
      .par // Go parallel here, so that we can run the afteralls concurrently
      .foreach(afterAllsAndFutures => {
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
      runOrIgnoreValidChild
    }
  }

  private[havarunner] val classesToTest = withSubclasses(parentClass)

  private[havarunner] lazy val children: java.lang.Iterable[TestAndParameters] =
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

  private def runOrIgnoreValidChild(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description, executor: ForkJoinPool): Option[FutureTask[_]] =
    if (testAndParameters.ignored) {
      notifier fireTestIgnored description
      None
    } else {
      run
    }

  def run(implicit notifier: RunNotifier, description: Description, testAndParameters: TestAndParameters, executor: ForkJoinPool): Some[FutureTask[None.type]] = {
    val testTask = new FutureTask(new Runnable {
      def run() {
        try {
          notifier fireTestStarted description
          withThrottle(testOperation)
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

  private def afterAlls(implicit testAndParameters: TestAndParameters): Operation[Unit] =
    Operation(() =>
      testAndParameters.afterAll.foreach(invoke(_, testAndParameters))
    )

  private def testOperation(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description): Operation[Any] =
    Operation(() => {
      runWithRules {
        runTest
      }
    })

  private def runWithRules(f: => Any)(implicit testAndParameters: TestAndParameters) {
    val inner = new Statement {
      def evaluate() {
        f
      }
    }
    val withRulesApplied = testAndParameters
      .rules
      .foldLeft(inner) {
      (accumulator: Statement, rule: Field) => {
        try {
          val testRule: TestRule = rule.get(fromTestInstanceCache(testAndParameters)).asInstanceOf[TestRule]
          testRule.apply(accumulator, describeChild)
        } catch {
          case e: InvocationTargetException =>
            if (e.getCause.getClass == classOf[AssumptionViolatedException]) inner
            else throw e
        }
      }
    }
    withRulesApplied.evaluate()
  }

  private def runTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    maybeThrowingException {
      maybeTimeouting {
        testAndParameters.testMethod.invoke(fromTestInstanceCache(testAndParameters))
      }
    } match {
      case Some(exception) if exception.isInstanceOf[AssumptionViolatedException] =>
        val msg = s"[HavaRunner] Ignored $testAndParameters, because it did not meet an assumption"
        notifier fireTestAssumptionFailed new Failure(description, new AssumptionViolatedException(msg))
      case Some(exception) if testAndParameters.expectedException.isDefined =>
        if (exception.getClass == testAndParameters.expectedException.get) {
          // Expected exception. All ok.
        }
      case Some(exception) =>
        notifier fireTestFailure new Failure(description, exception)
      case None =>
        failIfExpectedExceptionNotThrown
    }
  }

  private def maybeTimeouting(op: => Any)(implicit testAndParameters: TestAndParameters) {
    testAndParameters.timeout.map(timeout => {
      val start = System.currentTimeMillis()
      op
      val duration = System.currentTimeMillis() - start
      if (duration >= timeout) {
        throw new RuntimeException(s"Test timed out after $duration milliseconds")
      }
    }).getOrElse({
      op
    })
  }

  private def failIfExpectedExceptionNotThrown(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    testAndParameters.expectedException.foreach(expected =>
      notifier fireTestFailure new Failure(description, new TestDidNotRiseExpectedException(testAndParameters.expectedException.get, testAndParameters))
    )
  }

  private def maybeThrowingException(testF: => Any)(implicit testAndParameters: TestAndParameters): Option[Throwable] =
    try {
      testF
      None
    } catch {
      case e: InvocationTargetException => Some(e.getTargetException)
      case e: Throwable => Some(e)
    }
}
