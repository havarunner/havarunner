package havarunner

import net.sf.cglib.proxy.{MethodProxy, MethodInterceptor, Enhancer}
import java.lang.reflect.Method
import org.junit.runners.model.Statement
import scala.collection.JavaConversions._
import havarunner.exception.ScenarioMethodNotFound

private[havarunner] object ScenarioHelper {
  def addScenarioInterceptor(testAndParameters: TestAndParameters, testClassInstance: Any) = {
    val interceptor = new ScenarioInterceptor()
    val enhancer = new Enhancer()
    enhancer.setSuperclass(testClassInstance.getClass)
    enhancer.setCallback(interceptor)
    val intercepted = enhancer.create()
    val testMethod = findScenarioTestMethod(testAndParameters, intercepted)
    withBefores(
      testRunningStatement(testAndParameters, intercepted, testMethod),
      testAndParameters,
      intercepted
    )
  }

  private def testRunningStatement(testAndParameters: TestAndParameters, intercepted: Object, testMethod: Method) =
    new Statement() {
      def evaluate() {
        testMethod.setAccessible(true)
        testMethod.invoke(intercepted, testAndParameters scenario)
      }
    }

  private def withBefores(statement: Statement, testAndParameters: TestAndParameters, intercepted: Any) =
    new Statement() {
      def evaluate() {
        testAndParameters.befores.foreach(before => {
          before.setAccessible(true)
          before.invoke(intercepted)
        })
        statement.evaluate()
      }
    }

  private def findScenarioTestMethod(testAndParameters: TestAndParameters, intercepted: Any) =
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

  class ScenarioInterceptor extends MethodInterceptor {
    def intercept(proxiedObject: Object, method: Method, args: Array[AnyRef], methodProxy: MethodProxy) =
      methodProxy.invokeSuper(proxiedObject, args)
  }
}