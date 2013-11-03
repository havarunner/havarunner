package com.github.havarunner.validations;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.exception.MultipleInvalidations;
import com.github.havarunner.exception.NonStaticInnerClassException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.havarunner.TestHelper.*;
import static org.junit.Assert.assertEquals;

public class MultipleInvalidationsTest {

    @Test
    public void HavaRunner_will_report_multiple_invalidations_at_once() {
        List<Failure> failures = runAndRecordFailures(new HavaRunner(TestWithMultipleInvalidations.class));
        expectNonStaticFailure(failures);
        expectMultipleInvalidations(failures);
    }

    private void expectMultipleInvalidations(List<Failure> failures) {
        Failure failure = findFailure(failures, MultipleInvalidations.class);
        assertEquals(
            "com.github.havarunner.exception.UnsupportedAnnotationException: Only tests that are @RunSequentially may use @After (class com.github.havarunner.validations.MultipleInvalidationsTest$TestWithMultipleInvalidations uses the unsupported annotation org.junit.After)\n" +
            "com.github.havarunner.exception.UnsupportedAnnotationException: Only tests that are @RunSequentially may use @Before (class com.github.havarunner.validations.MultipleInvalidationsTest$TestWithMultipleInvalidations uses the unsupported annotation org.junit.Before)\n" +
            "com.github.havarunner.exception.UnsupportedAnnotationException: class com.github.havarunner.validations.MultipleInvalidationsTest$TestWithMultipleInvalidations uses the unsupported annotation org.junit.BeforeClass",
            failure.getMessage()
        );
    }

    private void expectNonStaticFailure(List<Failure> failures) {
        Failure failure = findFailure(failures, NonStaticInnerClassException.class);
        assertEquals(
            "The class com.github.havarunner.validations.MultipleInvalidationsTest$TestWithMultipleInvalidations$InnerNonStatic must be static (HavaRunner does not support non-static inner classes)",
            failure.getMessage()
        );
    }

    static class TestWithMultipleInvalidations {

        @Before
        void before() { }

        @BeforeClass
        static void beforeClass() { }

        @After
        void after() { }

        @After
        void anotherAfter() { }

        @Test
        void test() {}

        class InnerNonStatic {

            @Test
            public void test() {}

        }
    }

    private Failure findFailure(List<Failure> failures, final Class type) {
        return Iterables.find(failures, new Predicate<Failure>() {
            @Override
            public boolean apply(@Nullable Failure input) {
                return input.getException().getClass().equals(type);
            }
        });
    }
}
