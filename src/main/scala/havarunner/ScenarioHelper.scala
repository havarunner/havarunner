package havarunner

import java.lang.reflect.Method
import scala.collection.JavaConversions._
import havarunner.exception.ScenarioMethodNotFound

private[havarunner] object ScenarioHelper {
  def createScenarioTestFunction(testAndParameters: TestAndParameters, testClassInstance: AnyRef): (() => Any) = {
    val testMethod = findScenarioTestMethod(testAndParameters, testClassInstance)
    withBefores(
      testRunningStatement(testAndParameters, testClassInstance, testMethod),
      testAndParameters,
      testClassInstance
    )
  }

  private def testRunningStatement(testAndParameters: TestAndParameters, intercepted: AnyRef, testMethod: Method) =
    () => {
        testMethod.setAccessible(true)
        testMethod.invoke(intercepted, testAndParameters scenario)
      }

  private def withBefores(runTest: (() => Any), testAndParameters: TestAndParameters, intercepted: AnyRef) =
    () => {
      testAndParameters.befores.foreach(before => {
        before.setAccessible(true)
        before.invoke(intercepted)
      })
      runTest()
    }

  private def findScenarioTestMethod(testAndParameters: TestAndParameters, intercepted: AnyRef) =
    try {
      val scenarioMethod = intercepted.
        getClass.
        getDeclaredMethod(
        testAndParameters.frameworkMethod.getName,
        testAndParameters.scenario.getClass
      )
      scenarioMethod
    } catch {
      case e: NoSuchMethodException =>
        val methodAndSignature = String.format(
          "%s(%s)",
          testAndParameters.frameworkMethod.getName,
          testAndParameters.scenario.getClass.getName
        )
        throw new ScenarioMethodNotFound(
          String.format(
            "Could not find the scenario method %s#%s. Please add the method %s into class %s.",
            testAndParameters.testClass.getJavaClass.getSimpleName,
            methodAndSignature,
            methodAndSignature,
            testAndParameters.testClass.getJavaClass.getName
          )
        )
    }

  val defaultScenario = new Object
}