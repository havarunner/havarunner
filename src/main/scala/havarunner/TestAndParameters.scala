package havarunner

import org.junit.runners.model.{TestClass, FrameworkMethod}
import java.lang.reflect.Method

private[havarunner] class TestAndParameters(
  val frameworkMethod: FrameworkMethod,
  val testClass: TestClass,
  val scenario: Object,
  val beforeClasses: Seq[Method],
  val befores: Seq[Method],
  val afters: Seq[Method],
  val afterClasses: Seq[Method],
  val runSequentially: Boolean
) {
  def scenarioToString = {
    if (scenario == ScenarioHelper.defaultScenario) {
      ""
    } else {
      s" (when ${scenario.toString})"
    }
  }
}
