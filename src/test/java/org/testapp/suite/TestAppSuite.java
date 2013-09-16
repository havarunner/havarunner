package org.testapp.suite;

import com.github.havarunner.HavaRunnerSuite;

public class TestAppSuite implements HavaRunnerSuite<String> {
    @Override
    public String suiteObject() {
        return "hello";
    }

    @Override
    public void afterSuite() {
    }
}
