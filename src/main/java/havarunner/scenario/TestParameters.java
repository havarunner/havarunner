package havarunner.scenario;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.Collection;

public class TestParameters {
    private final FrameworkMethod frameworkMethod;
    private final TestClass testClass;
    private final Object scenario;
    private final Collection<Method> befores;

    public TestParameters(
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

    public FrameworkMethod getFrameworkMethod() {
        return frameworkMethod;
    }

    public TestClass getTestClass() {
        return testClass;
    }

    public Object getScenario() {
        return scenario;
    }

    public Collection<Method> getBefores() {
        return befores;
    }

    public String scenarioToString() {
        if (scenario.equals(ScenarioHelper.defaultScenario)) {
            return "";
        } else {
            return " (when " + getScenario().toString() + ")";
        }
    }
}
