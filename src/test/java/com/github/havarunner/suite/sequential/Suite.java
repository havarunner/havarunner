package com.github.havarunner.suite.sequential;

import com.github.havarunner.HavaRunnerSuite;

public class Suite implements HavaRunnerSuite<String> {
    @Override
    public String suiteObject() {
        return "hello";
    }

    @Override
    public void afterSuite() {
    }
}
