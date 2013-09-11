package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.TestAndParameters;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class IgnoredTest {

    @Test
    public void it_records_ignored_tests_properly() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(ignored_examples.class);
        final AtomicReference<Description> expectedIgnoration = runAndCollectIgnored(havaRunner);
        assertEquals("this_test_is_ignored", expectedIgnoration.get().getMethodName());
    }

    private AtomicReference<Description> runAndCollectIgnored(HavaRunner havaRunner) {
        final AtomicReference<Description> expectedIgnoration = new AtomicReference<>();
        for (TestAndParameters f : havaRunner.children()) {
            havaRunner.runChild(f, new RunNotifier() {
                @Override
                public void fireTestIgnored(Description description) {
                    expectedIgnoration.set(description);
                }
            });
        }
        return expectedIgnoration;
    }

    static class ignored_examples {
        @Test
        @Ignore
        void this_test_is_ignored() {
        }
    }
}
