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
class HavaRunner(parentClass: Class[_ <: Any]) extends Runner with Filterable {

  private var filterOption: Option[Filter] = None // The Filterable API requires us to use a var

  def getDescription = {
    val description = Description.createSuiteDescription(parentClass)
    children.iterator() foreach (child => description.addChild(describeChild(child)))
    description
  }

  def run(notifier: RunNotifier) {
    reportIfSuite()
    val afterAllFutures = children
      .groupBy(_.scenarioAndClass)
      .map {
        case (scenarioAndClass, testsAndParameters) =>
          val testResults: Iterable[Future[Option[TestInstance]]] = testsAndParameters.flatMap(runChild(_, notifier))
          Future.sequence(testResults).map { (testInstanceOptions: Iterable[Option[TestInstance]]) =>
            val testInstances = testInstanceOptions.flatMap(instance => instance)
            testInstances.headOption map { testInstance =>
              testsAndParameters.head.afterAll.foreach(invoke(_)(testInstance))
            }
          }
      }

    waitAndHandleRestOfErrors(afterAllFutures)
  }

  def waitAndHandleRestOfErrors(afterAllFutures: Iterable[Future[Option[_]]]) {
    val allTests = Future.sequence(afterAllFutures)
    var failure: Option[Throwable] = None
    allTests onFailure {
      case t: Throwable => failure = Some(t)
    }
    Await.ready(allTests, 2 hours)
    failure.foreach(throw _) // If @AfterAll methods throw exceptions, rethrow them here
  }

  def filter(filter: Filter) {
    this.filterOption = Some(filter)
  }

  private[havarunner] def reportIfSuite() =
    children.filter(_.testContext.isInstanceOf[SuiteContext]).foreach(testAndParameters => {
      val suiteContext: SuiteContext = testAndParameters.testContext.asInstanceOf[SuiteContext]
      println(s"[HavaRunner] Running ${testAndParameters.testMethod} as a part of ${suiteContext.suiteClass.getName}")
    })

  private[havarunner] def runChild(implicit testAndParameters: TestAndParameters, notifier: RunNotifier): Option[Future[Option[TestInstance]]] = {
    implicit val description = describeChild
    val testIsInvalidReport = reportInvalidations
    if (testIsInvalidReport.isDefined) {
      notifier fireTestFailure  new Failure(description, testIsInvalidReport.get)
      None
    } else {
      scheduleOrIgnore
    }
  }

  private[havarunner] val classesToTest = findDeclaredClasses(parentClass)

  private[havarunner] lazy val children: java.lang.Iterable[TestAndParameters] =
    parseTestsAndParameters(classesToTest).filter(acceptChild(_, filterOption))
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

  private def scheduleOrIgnore(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description): Option[Future[Option[TestInstance]]] =
    if (testAndParameters.ignored) {
      notifier fireTestIgnored description
      None
    } else {
      Some(schedule)
    }

  private def schedule(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description): Future[Option[TestInstance]] =
    testInstance map {
      implicit testInstance =>
        withThrottle {
          runWithRules {
            runTest
          }
          Some(testInstance)
        }
    } recover {
      case t: Throwable =>
        handleException(t) // We come here when the test instance constructor throws an exception
        None
    }

  private def runWithRules(f: => Any)(implicit testAndParameters: TestAndParameters, testInstance: TestInstance) {
    val inner = new Statement {
      def evaluate() {
        f
      }
    }
    def applyRuleAndHandleException(rule: Field, accumulator: Statement) =
      try {
        val testRule: TestRule = rule.get(testInstance.instance).asInstanceOf[TestRule]
        testRule.apply(accumulator, describeChild)
      } catch {
        case e: InvocationTargetException =>
          if (e.getCause.getClass == classOf[AssumptionViolatedException]) inner
          else {
            println(e.getMessage)
            throw e
          }
      }
    val foldedRules = testAndParameters
      .rules
      .foldLeft(inner) {
        (accumulator: Statement, rule: Field) => 
          applyRuleAndHandleException(rule, accumulator)
      }
    foldedRules.evaluate()
  }

  private def runTest(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description, testInstance: TestInstance) {
    notifier fireTestStarted description
    try {
      invokeEach(testAndParameters.before)
      maybeTimeouting { testAndParameters.testMethod.invoke(testInstance.instance)}
      failIfExpectedExceptionNotThrown
    } catch {
      case e: Throwable => handleException(e)
    } finally {
      try {
        invokeEach(testAndParameters.after)
      } finally {
        notifier fireTestFinished description
      }
    }
  }

  private def handleException(e: Throwable)(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    Option(e) match {
      case Some(exception) if exception.isInstanceOf[AssumptionViolatedException] =>
        val msg = s"[HavaRunner] Ignored $testAndParameters, because it did not meet an assumption"
        notifier fireTestAssumptionFailed new Failure(description, new AssumptionViolatedException(msg))
      case Some(exception) if testAndParameters.expectedException.isDefined =>
        if (exception.getClass == testAndParameters.expectedException.get) {
          // Expected exception. All ok.
        }
      case Some(exception) if exception.isInstanceOf[InvocationTargetException] =>
         handleException(exception.asInstanceOf[InvocationTargetException].getTargetException)
      case Some(exception) =>
        notifier fireTestFailure new Failure(description, exception)
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
    }).getOrElse(op)
  }

  private def failIfExpectedExceptionNotThrown(implicit testAndParameters: TestAndParameters, notifier: RunNotifier, description: Description) {
    testAndParameters.expectedException.foreach(expected =>
      notifier fireTestFailure new Failure(description, new TestDidNotRiseExpectedException(testAndParameters.expectedException.get, testAndParameters))
    )
  }
}
