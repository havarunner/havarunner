package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunnerSuite;

public class SuiteWithMemberThatHasScenarios implements HavaRunnerSuite<String> {
    @Override
    public String suiteObject() {
        return "foo";
    }

    @Override
    public void afterSuite() {
    }
}
