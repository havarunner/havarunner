package havarunner;


import havarunner.exception.UnsupportedAnnotationException;
import org.junit.After;
import org.junit.Test;

import static havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UnsupportedJUnitAnnotationsTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_when_a_test_uses_an_unsupported_junit_annotation() {
        HavaRunner havaRunner = new HavaRunner(TestWithUnsupportedJUnitAnnotation.class);
        try {
            run(havaRunner);
            fail("Must throw an exception");
        } catch (UnsupportedAnnotationException e) {
            assertEquals(e.annotationClass(), After.class);
            assertEquals(
                    "class havarunner.UnsupportedJUnitAnnotationsTest$TestWithBeforeClass uses the unsupported annotation org.junit.After",
                    e.getMessage()
            );
        }
    }

    static class TestWithUnsupportedJUnitAnnotation {
        @After
        void HavaRunner_does_not_support_the_After_annotation() {
        }

        @Test
        void this_test_will_not_be_run_because_the_BeforeClass_annotation_is_present() {

        }
    }
}
