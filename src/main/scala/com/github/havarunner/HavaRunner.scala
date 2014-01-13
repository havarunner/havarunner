package com.github.havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.RunNotifier
import scala.collection.JavaConversions._
import Validations._
import java.lang.reflect.{Field, InvocationTargetException}
import org.junit.runner.manipulation.{Filter, Filterable}
import com.github.havarunner.HavaRunner._
import com.github.havarunner.exception.TestDidNotRiseExpectedException
import com.github.havarunner.Parser._
import com.github.havarunner.Reflections._
import org.junit.internal.AssumptionViolatedException
import com.github.havarunner.TestInstanceCache._
import com.github.havarunner.ConcurrencyControl._
import com.github.havarunner.ExceptionHelper._
import com.github.havarunner.RunnerHelper._
import org.junit.runners.model.Statement
import org.junit.rules.TestRule
import org.junit.runner.notification.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

/**
 * Usage: @org.junit.runner.RunWith(HavaRunner.class)
 *
 * @author Lauri Lehmijoki
 */
class HavaRunner(startingPoint: Class[_]) extends Runner with Filterable {

  private var filterOption: Option[Filter] = None // The Filterable API requires us to use a var

  def getDescription = {
    val description = Description.createSuiteDescription(startingPoint)
    tests.iterator() foreach (test => description.addChild(describeTest(test)))
    description
  }

  def run(notifier: RunNotifier) {
    reportIfSuite(tests).foreach(println)
    val afterAllFutures = tests
      .groupBy(_.groupCriterion)
      .map {
        case (_, testsAndParameters) =>
          implicit val runNotifier = notifier
          runTestsOfSameGroup(testsAndParameters)
      }

    waitAndHandleRestOfErrors(afterAllFutures)
  }

  def filter(filter: Filter) {
    this.filterOption = Some(filter)
  }

  private[havarunner] val classesToTest = findDeclaredClasses(startingPoint)

  private[this] implicit lazy val parseResult: ParseResult = tests.toSeq

  private[havarunner] implicit lazy val tests: java.lang.Iterable[TestAndParameters] =
    parseTestsAndParameters(classesToTest).filter(acceptTest(_, filterOption))
}

/**
 * Place here code that is directly related to running the tests.
 */
private object HavaRunner {

  def runTestsOfSameGroup(testsAndParameters: Iterable[TestAndParameters])(implicit notifier: RunNotifier, parseResult: ParseResult): Future[Any] = {
    val runnableTests = handleIgnoredAndInvalid(testsAndParameters)
    val resultsOfSameGroup: Iterable[Future[Either[FailedConstructor, InstantiatedTest]]] = runnableTests.map(implicit tp => schedule)
    Future.sequence(resultsOfSameGroup) map {
      testResults => runAfterAlls(testResults)(runnableTests)
    }
  }

  def runAfterAlls(result: Iterable[Either[FailedConstructor, InstantiatedTest]])(implicit testsAndParameters: Iterable[TestAndParameters]) {
    result
      .map(_.right)
      .flatMap(_.toOption)
      .headOption
      .foreach(instantiatedTest => {
        implicit val testAndParams: TestAndParameters = testsAndParameters.head
        withThrottle {
          // It suffices to run the @AfterAlls against any instance of the group
          testAndParams.afterAll.foreach(invoke(_)(instantiatedTest.testInstance))
        }
      })
  }

  def waitAndHandleRestOfErrors(afterAllFutures: Iterable[Future[Any]]) {
    val allTests = Future.sequence(afterAllFutures)
    var failure: Option[Throwable] = None
    allTests onFailure {
      case t: Throwable => failure = Some(t) // Unlift the exception from the Future container, so that we can handle it in the main thread
    }
    Await.result(allTests, 2 hours)
    failure.foreach(throw _) // If @AfterAll methods throw exceptions, re-throw them here
  }
  
  def handleIgnoredAndInvalid(testsAndParameters: Iterable[TestAndParameters])(implicit notifier: RunNotifier) = {
    val ignoredTests = testsAndParameters.filter(_.ignored)
    ignoredTests.foreach(ignoredTest =>
      notifier fireTestIgnored describeTest(ignoredTest)
    )
    val invalidTests = testsAndParameters.filterNot(reportInvalidations(_).isEmpty)
    invalidTests.foreach(implicit invalidTest =>
      reportFailure(reportInvalidations)(describeTest, notifier)
    )

    testsAndParameters
      .filterNot(ignoredTests.contains(_))
      .filterNot(invalidTests.contains(_))
  }

  def schedule(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, parseResult: ParseResult):
  Future[Either[FailedConstructor, InstantiatedTest]] =
    future {
      withThrottle {
        implicit val instance = instantiateTestClass
        notifier fireTestStarted describeTest
        try {
          runWithRules {
            runTest
          }
        } catch {
          case error: Throwable =>
            handleException(error)
        } finally {
          notifier fireTestFinished describeTest
        }
        Right(InstantiatedTest(instance))
      }
    } recover {
      case errorFromConstructor: Throwable =>
        handleException(errorFromConstructor) // We come here when instantiating the test object failed
        Left(FailedConstructor())
    }

  def runWithRules(test: => Any)(implicit testAndParameters: TestAndParameters, testInstance: TestInstance) {
    val inner = new Statement {
      def evaluate() {
        test
      }
    }
    def applyRuleAndHandleException(rule: Field, accumulator: Statement) = {
      val testRule: TestRule = ensureAccessible(rule).get(testInstance.instance).asInstanceOf[TestRule]
      testRule.apply(accumulator, describeTest)
    }
    val foldedRules =
      testAndParameters
      .rules
      .foldLeft(inner) {
        (accumulator: Statement, rule: Field) =>
          applyRuleAndHandleException(rule, accumulator)
      }
    foldedRules.evaluate()
  }

  def runTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, testInstance: TestInstance) {
    try {
      testAndParameters.before.foreach(invoke)
      maybeTimeouting { ensureAccessible(testAndParameters.testMethod).invoke(testInstance.instance)}
      failIfExpectedExceptionNotThrown
    } finally {
      testAndParameters.after.foreach(invoke)
    }
  }

  def handleException(e: Throwable)(implicit testAndParameters: TestAndParameters, notifier: RunNotifier) {
    Option(e) match {
      case Some(exception) if exception.isInstanceOf[AssumptionViolatedException] =>
        val msg = s"[HavaRunner] Ignored $testAndParameters, because it did not meet an assumption"
        notifier fireTestAssumptionFailed new Failure(describeTest, new AssumptionViolatedException(msg))
      case Some(exception) if testAndParameters.expectedException.isDefined =>
        if (exception.getClass == testAndParameters.expectedException.get) {
          // Expected exception. All ok.
        }
      case Some(exception) if exception.isInstanceOf[InvocationTargetException] =>
         handleException(exception.asInstanceOf[InvocationTargetException].getTargetException)
      case Some(exception) =>
        notifier fireTestFailure new Failure(describeTest, exception)
    }
  }

  def maybeTimeouting(op: => Any)(implicit testAndParameters: TestAndParameters) {
    testAndParameters.timeout.map(timeout => {
      val start = System.currentTimeMillis()
      op
      val duration = System.currentTimeMillis() - start
      if (duration >= timeout) {
        throw new RuntimeException(s"Test timed out after $duration milliseconds")
      }
    }).getOrElse(op)
  }

  def failIfExpectedExceptionNotThrown(implicit testAndParameters: TestAndParameters, notifier: RunNotifier) {
    testAndParameters.expectedException.foreach(expected =>
      notifier fireTestFailure new Failure(describeTest, new TestDidNotRiseExpectedException(testAndParameters.expectedException.get, testAndParameters))
    )
  }

  case class FailedConstructor()
  case class InstantiatedTest(testInstance: TestInstance)
}
