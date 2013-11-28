package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

public class WhenAfterFails {

    @Test
    public void HavaRunner_will_mark_the_test_as_finished() {
        final AtomicReference<Boolean> finished = new AtomicReference<>(false);
        new HavaRunner(AfterFails.class).run(new RunNotifier() {
            @Override
            public void fireTestFinished(Description description) {
                finished.set(true);
            }
        });
        assertTrue(finished.get());
    }

    @RunSequentially
    static class AfterFails {

        @Test
        public void test() { }

        @After
        public void fail() {
            throw new RuntimeException("Fail intentionally");
        }
    }
}
