package com.github.havarunner.suite.sequential;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class SequentialSuiteMembersTest {

    static Thread suiteMemberThread;

    @Test
    public void HavaRunner_runs_suite_members_sequentially_if_they_contain_the_RunSequentially_annotation() {
        run(new HavaRunner(Suite.class));
        assertEquals("main", suiteMemberThread.getName());
        System.out.println(suiteMemberThread);
    }
}
