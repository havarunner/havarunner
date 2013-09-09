package havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent.{TimeUnit, SynchronousQueue, ThreadPoolExecutor}
import scala.collection.JavaConversions._
import havarunner.HavaRunnerHelper._
import havarunner.CodingConventions._
import havarunner.ScenarioHelper._
import org.junit.runners.model.Statement
import org.junit.{Test, Ignore}
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException

class HavaRunner(parentClass: Class[_ <: Any]) extends Runner {
  val executor = new ThreadPoolExecutor(
    0, Runtime.getRuntime.availableProcessors(),
    60L, TimeUnit.SECONDS,
    new SynchronousQueue[Runnable],
    new ThreadPoolExecutor.CallerRunsPolicy()
  )

  val classesToTest = Seq(
    parentClass
  ) ++ parentClass.getDeclaredClasses

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


  def getChildren: java.lang.Iterable[TestAndParameters] = toTestParameters(classesToTest)

  def runChild(testAndParameters: TestAndParameters, notifier: RunNotifier) {
    val description = describeChild(testAndParameters)
    val codingConventionException = violatesCodingConventions(
      testAndParameters,
      testAndParameters.testClass
    )
    if (codingConventionException.isDefined)
      notifier fireTestAssumptionFailed new Failure(description, codingConventionException.get)
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
      executor submit new Runnable {
        def run() {
          runLeaf(
            toStatement(testAndParameters),
            description,
            notifier
          )
        }
      }
    }
  }

  private def runLeaf(statement: Statement, description: Description, notifier: RunNotifier) {
    val eachNotifier = new EachTestNotifier(notifier, description)
    eachNotifier fireTestStarted()
    try {
      statement evaluate()
    } catch {
      case e: AssumptionViolatedException => eachNotifier addFailedAssumption e
      case e: Throwable => eachNotifier addFailure e
    } finally {
      eachNotifier fireTestFinished()
    }
  }

  private def toStatement(testAndParameters: TestAndParameters) = {
    new Statement { // TODO replace statements with plain functions
      def evaluate() {
        withExpectedExceptionTolerance(
          createTestInvokingStatement
        ).evaluate()
      }

      def createTestInvokingStatement: Statement = {
        val testClassInstance = CodeGeneratorHelper.newEnhancedInstance(testAndParameters.testClass.getJavaClass)
        if (isScenarioClass(testAndParameters.testClass.getJavaClass))
          runScenarioTest(testAndParameters, testClassInstance)
        else
          new Statement() {
            def evaluate() {
              testAndParameters.befores foreach (before => {
                before setAccessible true
                before invoke testClassInstance
              })
              testAndParameters.frameworkMethod.invokeExplosively(testClassInstance)
            }
          }
      }

      def withExpectedExceptionTolerance(testInvokingStatement: Statement) = {
        new Statement() {
          def evaluate() {
            try {
              testInvokingStatement evaluate()
            } catch {
              case exceptionWhileRunningTest: Throwable =>
                val annotation = testAndParameters.frameworkMethod.getAnnotation(classOf[Test])
                val expectedException = annotation.expected
                if (!(expectedException isAssignableFrom exceptionWhileRunningTest.getClass)) {
                  throw exceptionWhileRunningTest
                }
            }
          }
        }
      }
    }
  }
}
