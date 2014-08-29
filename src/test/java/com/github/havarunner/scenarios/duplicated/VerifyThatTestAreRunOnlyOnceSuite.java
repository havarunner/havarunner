package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.HavaRunnerSuite;

import java.util.concurrent.atomic.AtomicInteger;


abstract class VerifyThatTestAreRunOnlyOnceSuite implements HavaRunnerSuite<AtomicInteger>{
    final AtomicInteger integer = new AtomicInteger(0);
    @Override
    public AtomicInteger suiteObject() {
        return integer;
    }

    @Override
    public void afterSuite() {

    }
}
