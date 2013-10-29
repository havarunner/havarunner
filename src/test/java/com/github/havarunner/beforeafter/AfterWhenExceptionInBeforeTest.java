package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.runAndIgnoreErrors;
import static org.junit.Assert.assertTrue;

public class AfterWhenExceptionInBeforeTest {

    static boolean afterCalled;

    @Test
    public void HavaRunner_the_after_method_even_if_the_before_call_fails() {
        runAndIgnoreErrors(new HavaRunner(SequentialTest.class));
        assertTrue(afterCalled);
    }

    @RunSequentially
    static class SequentialTest {
        @Before
        void before() throws InterruptedException {
            throw new RuntimeException();
        }

        @Test
        void test() {
        }

        @After
        void after() throws InterruptedException {
            afterCalled = true;
        }
    }
}
