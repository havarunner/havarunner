package com.github.havarunner;

import com.github.havarunner.annotation.RunSequentially;
import com.github.havarunner.exception.UnsupportedAnnotationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class UnsupportedJUnitAnnotationsTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_when_a_test_uses_an_unsupported_junit_annotation() {
        HavaRunner havaRunner = new HavaRunner(TestWithUnsupportedJUnitAnnotation.class);
        UnsupportedAnnotationException report = (UnsupportedAnnotationException) runAndRecordFailure(havaRunner).getException();
        assertEquals(report.annotationClass(), Before.class);
        assertEquals(
            "class com.github.havarunner.UnsupportedJUnitAnnotationsTest$TestWithUnsupportedJUnitAnnotation uses the unsupported annotation org.junit.Before",
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

    static class TestWithUnsupportedJUnitAnnotation {
        @Before
        void HavaRunner_does_not_support_the_After_annotation() {
        }

        @Test
        void this_test_will_not_be_run_because_the_BeforeClass_annotation_is_present() {

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
