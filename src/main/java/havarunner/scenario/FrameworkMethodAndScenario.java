package havarunner.scenario;

import org.junit.runners.model.FrameworkMethod;

public class FrameworkMethodAndScenario {
    private final FrameworkMethod frameworkMethod;
    private final Object scenario;

    public FrameworkMethodAndScenario(FrameworkMethod frameworkMethod, Object scenario) {
        this.frameworkMethod = frameworkMethod;
        this.scenario = scenario;
    }

    public FrameworkMethod getFrameworkMethod() {
        return frameworkMethod;
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
