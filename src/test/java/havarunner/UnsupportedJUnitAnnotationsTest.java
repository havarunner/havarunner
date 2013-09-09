package havarunner;


import havarunner.exception.UnsupportedAnnotationException;
import org.junit.BeforeClass;
import org.junit.Test;

import static havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class UnsupportedJUnitAnnotationsTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_when_a_test_uses_an_unsupported_junit_annotation() {
        HavaRunner havaRunner = new HavaRunner(TestWithBeforeClass.class);
        try {
            run(havaRunner);
        } catch (UnsupportedAnnotationException e) {
            assertEquals(e.annotationClass(), BeforeClass.class);
            assertEquals(
                    "class havarunner.UnsupportedJUnitAnnotationsTest$TestWithBeforeClass uses the unsupported annotation org.junit.BeforeClass",
                    e.getMessage()
            );
        }
    }

    static class TestWithBeforeClass {
        @BeforeClass
        void HavaRunner_does_not_support_the_BeforeClass_annotation() {
        }

        @Test
        void this_test_will_not_be_run_because_the_BeforeClass_annotation_is_present() {

        }
    }
}
