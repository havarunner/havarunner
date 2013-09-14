package com.github.havarunner.suite;

import com.github.havarunner.HavaRunnerSuite;
import org.junit.Test;

class ExampleSuite implements HavaRunnerSuite<String> {

    public String suiteObject() {
        return "hello suite";
    }

    public void afterSuite() {
    }

    @Test
    void a_suite_may_also_contain_tests() {
        RunningSuiteTest.when_running_the_whole_suite.suiteTestCalled = true;
    }
}
