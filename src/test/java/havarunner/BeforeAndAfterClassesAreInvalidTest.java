package havarunner;

import havarunner.HavaRunner;
import havarunner.exception.MethodIsNotStatic;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static havarunner.TestHelper.runAndRecordFailedAssumption;
import static junit.framework.Assert.assertEquals;

public class BeforeAndAfterClassesAreInvalidTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_message_if_the_BeforeClass_method_is_not_static() {
        AtomicReference<Throwable> report = runAndRecordFailedAssumption(new HavaRunner(BeforeClassNotStatic.class));
        assertEquals(report.get().getClass(), MethodIsNotStatic.class);
        assertEquals("Method BeforeClassNotStatic#this_is_an_invalid_BeforeClass_method should be static", report.get().getMessage());
    }

    @Test
    public void HavaRunner_gives_a_helpful_error_message_if_the_AfterClass_method_is_not_static() {
        AtomicReference<Throwable> report = runAndRecordFailedAssumption(new HavaRunner(AfterClassNotStatic.class));
        assertEquals(report.get().getClass(), MethodIsNotStatic.class);
        assertEquals("Method AfterClassNotStatic#this_is_an_invalid_AfterClass_method should be static", report.get().getMessage());
    }

    static class AfterClassNotStatic {
        @AfterClass
        void this_is_an_invalid_AfterClass_method() {

        }

        @Test
        void will_not_be_called_because_of_the_invalid_method_above() {
            throw new RuntimeException("This should not be called");
        }
    }

    static class BeforeClassNotStatic {
        @BeforeClass
        void this_is_an_invalid_BeforeClass_method() {

        }

        @Test
        void will_not_be_called_because_of_the_invalid_method_above() {
            throw new RuntimeException("This should not be called");

        }
    }
}
