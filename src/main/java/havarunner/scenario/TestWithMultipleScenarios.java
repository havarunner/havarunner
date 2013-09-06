package havarunner.scenario;

import java.util.Set;

public interface TestWithMultipleScenarios<M> {
    /**
     * @return all the scenarios in which the tests will be run
     */
    Set<M> scenarios();

    /**
     * @return scenario provided by {@link havarunner.HavaRunner}
     */
    M currentScenario();
}
