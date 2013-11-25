package com.github.havarunner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IgnoredTest {
    static boolean ignoredTestIsRun;
    static boolean testInIgnoredCLassIsRun;

    @Test
    public void it_records_ignored_tests_properly() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(ContainingIgnoredTest.class);
        final AtomicReference<Description> expectedIgnoration = runAndCollectIgnored(havaRunner);
        assertEquals("this_test_is_ignored", expectedIgnoration.get().getMethodName());
        assertFalse(ignoredTestIsRun);
    }

    @Test
    public void it_records_ignored_classes_properly() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(IgnoredClass.class);
        final AtomicReference<Description> expectedIgnoration = runAndCollectIgnored(havaRunner);
        assertEquals("the_class_of_this_test_is_ignored", expectedIgnoration.get().getMethodName());
        assertFalse(testInIgnoredCLassIsRun);
    }

    @Test
    public void it_supports_Ignore_in_enclosing_class() {
        HavaRunner havaRunner = new HavaRunner(IgnoredEnclosing.class);
        assertTrue(havaRunner.tests().iterator().next().ignored());
    }

    private AtomicReference<Description> runAndCollectIgnored(HavaRunner havaRunner) {
        final AtomicReference<Description> expectedIgnoration = new AtomicReference<>();
        for (TestAndParameters f : havaRunner.tests()) {
            HavaRunner.validateAndRun(f, new RunNotifier() {
                @Override
                public void fireTestIgnored(Description description) {
                    expectedIgnoration.set(description);
                }
            });
        }
        return expectedIgnoration;
    }

    static class ContainingIgnoredTest {
        @Test
        @Ignore
        void this_test_is_ignored() {
            ignoredTestIsRun = true;
        }
    }

    @Ignore
    static class IgnoredClass {

        @Test
        void the_class_of_this_test_is_ignored() {
            testInIgnoredCLassIsRun = true;
        }
    }

    @Ignore
    static class IgnoredEnclosing {
       static class InnerClass {
           @Test
           void the_class_of_this_test_is_ignored() {
           }
       }
    }
}
