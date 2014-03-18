package com.github.havarunner.suite.covariant;

import com.github.havarunner.HavaRunnerSuite;


public class Suite implements HavaRunnerSuite<SuiteObject> {
    @Override
    public SuiteObject suiteObject() {
        return new SuiteObjectImpl();
    }

    @Override
    public void afterSuite() {

    }
}
