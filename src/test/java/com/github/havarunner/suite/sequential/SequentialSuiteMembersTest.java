package com.github.havarunner.suite.sequential;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.TestAndParameters;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SequentialSuiteMembersTest {

    @Test
    public void HavaRunner_runs_suite_members_sequentially_if_they_contain_the_RunSequentially_annotation() {
        for (TestAndParameters testAndParameters : new HavaRunner(Suite.class).children()) {
            assertTrue(testAndParameters.runSequentially().isDefined());
        }
    }
}
