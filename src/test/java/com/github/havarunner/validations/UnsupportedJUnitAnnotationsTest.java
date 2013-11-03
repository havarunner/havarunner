package com.github.havarunner.validations;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import com.github.havarunner.exception.UnsupportedAnnotationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class UnsupportedJUnitAnnotationsTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_a_parallel_test_uses_an_annotation_that_may_only_be_used_with_sequential_tests() {
        HavaRunner havaRunner = new HavaRunner(ParallelTestWithBefore.class);
        UnsupportedAnnotationException report = (UnsupportedAnnotationException) runAndRecordFailure(havaRunner).getException();
        assertEquals(report.annotationClass(), Before.class);
        assertEquals(
            "Only tests that are @RunSequentially may use @Before (class com.github.havarunner.validations.UnsupportedJUnitAnnotationsTest$ParallelTestWithBefore uses the unsupported annotation org.junit.Before)",
            report.getMessage()
        );
    }

    @Test
    public void HavaRunner_gives_a_helpful_error_message_when_a_test_uses_an_unsupported_junit_annotation() {
        HavaRunner havaRunner = new HavaRunner(ParallelTestWithAfterClass.class);
        UnsupportedAnnotationException report = (UnsupportedAnnotationException) runAndRecordFailure(havaRunner).getException();
        assertEquals(report.annotationClass(), AfterClass.class);
        assertEquals(
            "class com.github.havarunner.validations.UnsupportedJUnitAnnotationsTest$ParallelTestWithAfterClass uses the unsupported annotation org.junit.AfterClass",
            report.getMessage()
        );
    }

    @Test
    public void sequential_tests_may_use_Before() {
        run(new HavaRunner(SequentialTest.class));
    }

    @Test
    public void sequential_tests_may_use_After() {
        run(new HavaRunner(SequentialTest.class));
    }

    static class ParallelTestWithBefore {
        @Before
        void before() {
        }

        @Test
        void test() {

        }
    }

    static class ParallelTestWithAfterClass {
        @Test
        void test() {

        }

        @AfterClass
        static void after() {
        }
    }


    @RunSequentially
    static class SequentialTest {
        @Before
        void before() {
        }

        @After
        void after() {
        }

        @Test
        void test() {

        }
    }
}
