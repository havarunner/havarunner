package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class RunningSuiteTest {

    public static class when_running_the_whole_suite {
        static boolean suiteTestCalled;
        static boolean suiteMember1Called;
        static boolean suiteMember2Called;

        @Test
        public void HavaRunner_calls_all_the_suite_members_when_running_the_suite() {
            run(new HavaRunner(ExampleSuite.class));
            assertTrue(suiteTestCalled);
            assertTrue(suiteMember1Called);
            assertTrue(suiteMember2Called);
        }
    }
}
