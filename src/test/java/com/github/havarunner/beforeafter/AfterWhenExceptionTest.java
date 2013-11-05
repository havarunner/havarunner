package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndIgnoreErrors;
import static org.junit.Assert.assertTrue;

public class AfterWhenExceptionTest {

    static boolean afterCalled;

    @Test
    public void HavaRunner_the_after_method_even_if_the_test_fails() {
        runAndIgnoreErrors(new HavaRunner(SequentialTest.class));
        assertTrue(afterCalled);
    }

    @RunSequentially(because = "this test does not thrive in the concurrent world")
    static class SequentialTest {

        @Test
        void test() {
            throw new RuntimeException("Test fails");
        }

        @After
        void after() throws InterruptedException {
            afterCalled = true;
        }
    }
}
