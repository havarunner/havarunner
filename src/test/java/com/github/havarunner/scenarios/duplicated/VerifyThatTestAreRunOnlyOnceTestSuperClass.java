package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.annotation.AfterAll;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

abstract class VerifyThatTestAreRunOnlyOnceTestSuperClass {
    protected final AtomicInteger suiteObject;
    protected final List<String> scenarios;
    VerifyThatTestAreRunOnlyOnceTestSuperClass(AtomicInteger suiteObject, List<String> scenarios) {
        this.suiteObject = suiteObject;
        this.scenarios = scenarios;
    }
    @Test
    public void verifyTestIsRanOnlyOncePerScenario() {
        final int runNumber = suiteObject.addAndGet(1);
        System.out.println("Run: "+runNumber+ " for scenario: "+scenarios);
        synchronized (VerifyThatTestAreRunOnlyOnceTestOneImpl.class) {
            assertTrue(runNumber+" / "+VerifyThatTestAreRunOnlyOnceTestOneImpl.scenarios().size()+ " currentScenario: "+scenarios, VerifyThatTestAreRunOnlyOnceTestOneImpl.scenarios().size() >= runNumber);
        }
    }

    @AfterAll
    public void afterAll() {

    }


}
