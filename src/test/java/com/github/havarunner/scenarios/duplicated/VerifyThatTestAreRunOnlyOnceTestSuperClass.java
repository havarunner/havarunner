package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.annotation.AfterAll;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

abstract class VerifyThatTestAreRunOnlyOnceTestSuperClass {
    protected final AtomicInteger suiteObject;
    protected final List<String> scenarios;
    /**
     * When HavaRunner tests are executed via SBT, both VerifyThatTestAreRunOnlyOnceSuiteImpl and VerifyThatTestAreRunOnlyOnceTestOneImpl are executed,
     * resulting in tests running twice, thus failing the java8 assertions here..
     *
     * This flag avoids that by making sure that we only run our suite once.
     */
    protected static boolean hasExecuted = false;
    VerifyThatTestAreRunOnlyOnceTestSuperClass(AtomicInteger suiteObject, List<String> scenarios) {
        this.suiteObject = suiteObject;
        this.scenarios = scenarios;
    }
    @Test
    public void verifyTestIsRanOnlyOncePerScenario() {

        final int runNumber = suiteObject.addAndGet(1);
        System.out.println("Run: "+runNumber+ " for scenario: "+scenarios);
        synchronized (VerifyThatTestAreRunOnlyOnceTestOneImpl.class) {
            assumeFalse(hasExecuted);
            assertTrue(runNumber+" / "+VerifyThatTestAreRunOnlyOnceTestOneImpl.scenarios().size()+ " currentScenario: "+scenarios, VerifyThatTestAreRunOnlyOnceTestOneImpl.scenarios().size() >= runNumber);
        }
    }
    @AfterAll
    public void markAsExecuted() {
        hasExecuted = true;
    }
}
