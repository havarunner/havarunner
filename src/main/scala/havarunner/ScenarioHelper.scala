package havarunner

import java.lang.reflect.Method
import havarunner.exception.ScenarioMethodNotFound

private[havarunner] object ScenarioHelper {
  def createScenarioTestFunction(testAndParameters: TestAndParameters, testClassInstance: AnyRef): Operation[AnyRef] = {
    val testMethodOption = findScenarioTestMethod(testAndParameters, testClassInstance)
    if (testMethodOption.isDefined) {
      testRunningStatement(testAndParameters, testClassInstance, testMethodOption.get)
    } else {
      scenarioMethodNotFoundStatement(testAndParameters)
    }
  }

  private def testRunningStatement(testAndParameters: TestAndParameters, intercepted: AnyRef, testMethod: Method): Operation[AnyRef] =
    Operation(() => {
      testMethod.setAccessible(true)
      testMethod.invoke(intercepted, testAndParameters scenario)
    })

  private def scenarioMethodNotFoundStatement(testAndParameters: TestAndParameters): Operation[AnyRef] = Operation(() => {
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
  })

  private def findScenarioTestMethod(testAndParameters: TestAndParameters, intercepted: AnyRef): Option[Method] =
    try {
      val scenarioMethod = intercepted.
        getClass.
        getDeclaredMethod(
        testAndParameters.frameworkMethod.getName,
        testAndParameters.scenario.getClass
      )
      Some(scenarioMethod)
    } catch {
      case e: NoSuchMethodException => None
    }

  val defaultScenario = new Object
}