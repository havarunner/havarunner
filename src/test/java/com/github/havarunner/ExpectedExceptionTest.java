package com.github.havarunner;

import com.github.havarunner.exception.TestDidNotRiseExpectedException;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class ExpectedExceptionTest {

    @Test
    public void HavaRunner_supports_the_expected_exception_in_the_Test_annotation() {
        Failure failure = runAndRecordFailure(new HavaRunner(TestWithExpectedException.class));
        assertNull(failure);
    }

    @Test
    public void HavaRunner_fails_the_test_if_the_expected_exception_is_not_thrown() {
        Failure failure = runAndRecordFailure(new HavaRunner(TestWithUnexpectedException.class));
        assertEquals(failure.getException().getClass(), TestDidNotRiseExpectedException.class);
        assertEquals(
            "Test com.github.havarunner.ExpectedExceptionTest$TestWithUnexpectedException#this_is_a_test_that_passes_unexpectedly did not throw the expected exception java.lang.UnsupportedOperationException",
            failure.getMessage()
        );
    }

    @Test
    public void HavaRunner_fails_if_the_test_throws_an_exception_that_is_not_the_expected_exception() {
        Failure failure = runAndRecordFailure(new HavaRunner(ExpectingOtherExceptionButGotAnother.class));
        assertEquals(failure.getException().getClass(), TestDidNotRiseExpectedException.class);
    }

    static class TestWithExpectedException {

        @Test(expected = IllegalStateException.class)
        void this_should_rise_an_exception() {
            throw new IllegalStateException("This is an expected exception");
        }
    }

    static class TestWithUnexpectedException {

        @Test(expected = UnsupportedOperationException.class)
        void this_is_a_test_that_passes_unexpectedly() {

        }
    }

    static class ExpectingOtherExceptionButGotAnother {

        @Test(expected = OtherExpection.class)
        public void shouldThrowOtherExpection() {
            throw new SomeException();
        }

        private static class SomeException extends RuntimeException {
        }

        private static class OtherExpection extends RuntimeException {
        }

    }
}
