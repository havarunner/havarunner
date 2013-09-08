package havarunner;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.Collection;

class TestAndParameters {
    final FrameworkMethod frameworkMethod;
    final TestClass testClass;
    final Object scenario;
    final Collection<Method> befores;

    TestAndParameters(
        FrameworkMethod frameworkMethod,
        TestClass testClass,
        Object scenario,
        Collection<Method> befores
    ) {
        this.frameworkMethod = frameworkMethod;
        this.testClass = testClass;
        this.scenario = scenario;
        this.befores = befores;
    }

     String scenarioToString() {
        if (scenario.equals(ScenarioHelper.defaultScenario)) {
            return "";
        } else {
            return " (when " + scenario.toString() + ")";
        }
    }
}
