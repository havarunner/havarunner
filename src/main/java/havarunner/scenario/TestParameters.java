package havarunner.scenario;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class TestParameters {
    private final FrameworkMethod frameworkMethod;
    private final TestClass testClass;
    private final Object scenario;

    public TestParameters(FrameworkMethod frameworkMethod, TestClass testClass, Object scenario) {
        this.frameworkMethod = frameworkMethod;
        this.testClass = testClass;
        this.scenario = scenario;
    }

    public FrameworkMethod getFrameworkMethod() {
        return frameworkMethod;
    }

    public TestClass getTestClass() {
        return testClass;
    }

    public Object getScenario() {
        return scenario;
    }

    public String scenarioToString() {
        if (scenario.equals(ScenarioHelper.defaultScenario)) {
            return "";
        } else {
            return " (when " + getScenario().toString() + ")";
        }
    }
}
