package havarunner

import org.junit.runners.model.{TestClass, FrameworkMethod}
import java.lang.reflect.Method

private[havarunner] class TestAndParameters(
  val frameworkMethod: FrameworkMethod,
  val testClass: TestClass,
  val scenario: Object,
  val befores: java.util.Collection[Method]) {
  def scenarioToString = {
    if (scenario == ScenarioHelper.defaultScenario) {
      ""
    } else {
      s" (when ${scenario.toString})"
    }
  }
}
