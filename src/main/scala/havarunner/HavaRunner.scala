package havarunner

import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{Failure, RunNotifier}
import java.util.concurrent.{TimeUnit, SynchronousQueue, ThreadPoolExecutor}
import scala.collection.JavaConversions._
import havarunner.HavaRunnerHelper._
import havarunner.CodingConventionsAndValidations._
import havarunner.ScenarioHelper._
import org.junit.{Test, Ignore}
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.AssumptionViolatedException
import java.lang.reflect.Method

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

  private[havarunner] val classesToTest = Seq(
    parentClass
  ) ++ parentClass.getDeclaredClasses

  private[havarunner] def getChildren: java.lang.Iterable[TestAndParameters] = toTestParameters(classesToTest)

  private[havarunner] def runChild(testAndParameters: TestAndParameters, notifier: RunNotifier) {
    val description = describeChild(testAndParameters)
    val codingConventionException = violatesCodingConventions(testAndParameters)
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
      val testClassInstance = CodeGeneratorHelper.newEnhancedInstance(testAndParameters.testClass.getJavaClass)
      val testOperation =
        runBeforeClasses(testAndParameters) andThen
          (runBefores(testAndParameters, testClassInstance) andThen
            runTest(testAndParameters, testClassInstance)) andThen
              runAfterClasses(testAndParameters)
      executor submit new Runnable {
        def run() {
          runLeaf(
            withExpectedExceptionTolerance(testAndParameters, testOperation),
            description,
            notifier
          )
        }
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

  private def withExpectedExceptionTolerance(testAndParameters: TestAndParameters, test: Operation[_ <: Any]): Operation[_ <: Any] = Operation(() => {
    try {
      test.run
    } catch {
      case exceptionWhileRunningTest: Throwable =>
        val annotation = testAndParameters.frameworkMethod.getAnnotation(classOf[Test])
        val expectedException = annotation.expected
        if (!(expectedException isAssignableFrom exceptionWhileRunningTest.getClass)) {
          throw exceptionWhileRunningTest
        } else
          Unit
    }
  })
}
