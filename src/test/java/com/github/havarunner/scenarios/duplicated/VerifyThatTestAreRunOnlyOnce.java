package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

@RunWith(HavaRunner.class)
@PartOf(VerifyThatTestAreRunOnlyOnceSuite.class)
public class VerifyThatTestAreRunOnlyOnce {
    final AtomicInteger suiteObject;
    final String scenario;
    VerifyThatTestAreRunOnlyOnce(AtomicInteger suiteObject, String scenario) {
        this.scenario = scenario;
        this.suiteObject = suiteObject;
    }

    @Test
    public void verifyTestIsRanOnlyOncePerScenario() {
        final int runNumber = suiteObject.addAndGet(1);
        System.out.println("Run: "+runNumber+ " for scenario: "+scenario);
        synchronized (VerifyThatTestAreRunOnlyOnce.class) {
            assertTrue(runNumber+" / "+scenarios().size()+ " currentScenario: "+scenario, scenarios().size() >= suiteObject.get());
        }
    }

    @Scenarios
    static List<String> scenarios() {
        return Lists.newArrayList("one", "two", "three", "four", "five", "six");
    }

}
