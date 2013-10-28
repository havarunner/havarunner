package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class AfterBeforeTimingTest {

    static long beforeCalled;
    static long testCalled;
    static long afterCalled;

    @Test
    public void HavaRunner_calls_first_before_then_test_and_finally_after() {
        run(new HavaRunner(SequentialTest.class));
        assertTrue(beforeCalled < testCalled);
        assertTrue(afterCalled > testCalled);
    }

    @RunSequentially
    static class SequentialTest {

        @Before
        void before() {
            beforeCalled = System.currentTimeMillis();
        }

        @Test
        void test() throws InterruptedException {
            Thread.sleep(1);
            testCalled = System.currentTimeMillis();
        }

        @After
        void after() throws InterruptedException {
            Thread.sleep(1);
            afterCalled = System.currentTimeMillis();
        }
    }
}
