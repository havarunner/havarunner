package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(HavaRunner.class)
public class VerifyThatTestAreRunOnlyOnceSuite implements HavaRunnerSuite<AtomicInteger>{
    final AtomicInteger integer = new AtomicInteger(0);
    @Override
    public AtomicInteger suiteObject() {
        return integer;
    }

    @Override
    public void afterSuite() {

    }
}
