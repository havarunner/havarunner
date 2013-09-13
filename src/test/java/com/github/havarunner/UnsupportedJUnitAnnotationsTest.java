package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.exception.UnsupportedAnnotationException;
import org.junit.Rule;
import org.junit.Test;

import static com.github.havarunner.TestHelper.runAndRecordFailedAssumption;
import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class UnsupportedJUnitAnnotationsTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_when_a_test_uses_an_unsupported_junit_annotation() {
        HavaRunner havaRunner = new HavaRunner(TestWithUnsupportedJUnitAnnotation.class);
        UnsupportedAnnotationException report = (UnsupportedAnnotationException) runAndRecordFailure(havaRunner).getException();
        assertEquals(report.annotationClass(), Rule.class);
        assertEquals(
            "class com.github.havarunner.UnsupportedJUnitAnnotationsTest$TestWithUnsupportedJUnitAnnotation uses the unsupported annotation org.junit.Rule",
            report.getMessage()
        );
    }

    static class TestWithUnsupportedJUnitAnnotation {
        @Rule
        void HavaRunner_does_not_support_the_After_annotation() {
        }

        @Test
        void this_test_will_not_be_run_because_the_BeforeClass_annotation_is_present() {

        }
    }
}
