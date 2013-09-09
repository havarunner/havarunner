package havarunner

import net.sf.cglib.proxy.{MethodProxy, MethodInterceptor, Enhancer}
import java.lang.reflect.Method
import org.junit.runners.model.Statement
import scala.collection.JavaConversions._
import havarunner.exception.ScenarioMethodNotFound

private[havarunner] object ScenarioHelper {
  def runScenarioTest(testAndParameters: TestAndParameters, testClassInstance: AnyRef) = {
    val testMethod = findScenarioTestMethod(testAndParameters, testClassInstance)
    withBefores(
      testRunningStatement(testAndParameters, testClassInstance, testMethod),
      testAndParameters,
      testClassInstance
    )
  }

  private def testRunningStatement(testAndParameters: TestAndParameters, intercepted: AnyRef, testMethod: Method) =
    new Statement() {
      def evaluate() {
        testMethod.setAccessible(true)
        testMethod.invoke(intercepted, testAndParameters scenario)
      }
    }

  private def withBefores(statement: Statement, testAndParameters: TestAndParameters, intercepted: AnyRef) =
    new Statement() {
      def evaluate() {
        testAndParameters.befores.foreach(before => {
          before.setAccessible(true)
          before.invoke(intercepted)
        })
        statement.evaluate()
      }
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