package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.AfterAll;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AfterAllFails {

    @Test
    public void HavaRunner_will_propagate_AfterAll_exceptions() {
        try {
            new HavaRunner(WithAfterAll.class).run(new RunNotifier());
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
        }
    }

    static class WithAfterAll {
        @Test
        void test() {}

        @AfterAll
        void afterAll() {
            throw new IllegalArgumentException("Expected");
        }
    }
}
