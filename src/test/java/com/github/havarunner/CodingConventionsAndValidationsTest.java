package com.github.havarunner;

import com.github.havarunner.exception.CamelCasedException;
import com.github.havarunner.exception.MemberIsNotPackagePrivateException;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.havarunner.TestHelper.runAndRecordFailedAssumption;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CodingConventionsAndValidationsTest {

    @Test
    public void HavaRunner_fails_if_a_test_method_is_camel_cased() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(CamelCaseExamples.class);
        AtomicReference<Throwable> expectedFailure = runAndRecordFailedAssumption(havaRunner);
        assertEquals(expectedFailure.get().getClass(), CamelCasedException.class);
    }

    @Test
    public void HavaRunner_requires_snake_cased_methods() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(snake_cases_examples.class);
        AtomicReference<Throwable> expectedFailure = runAndRecordFailedAssumption(havaRunner);
        assertNull(expectedFailure.get());
    }

    @Test
    public void HavaRunner_rejects_public_test_methods() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(public_modifier_example.class);
        AtomicReference<Throwable> expectedFailure = runAndRecordFailedAssumption(havaRunner);
        assertEquals(expectedFailure.get().getClass(), MemberIsNotPackagePrivateException.class);
    }

    @Test
    public void HavaRunner_rejects_private_test_methods() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(private_modifier_example.class);
        AtomicReference<Throwable> expectedFailure = runAndRecordFailedAssumption(havaRunner);
        assertEquals(expectedFailure.get().getClass(), MemberIsNotPackagePrivateException.class);
    }

    @Test
    public void HavaRunner_rejects_protected_test_methods() throws InitializationError {
        HavaRunner havaRunner = new HavaRunner(protected_modifier_example.class);
        AtomicReference<Throwable> expectedFailure = runAndRecordFailedAssumption(havaRunner);
        assertEquals(expectedFailure.get().getClass(), MemberIsNotPackagePrivateException.class);
    }

    static class CamelCaseExamples {

        @Test
        void camelCasedTest() { }
    }

    static class snake_cases_examples {

        @Test
        void snake_case_improves_readability() { }

        @Test
        void it_is_ok_to_combine_snake_case_with_CamelCase() { }

        void itIsOkToHaveCamelCaseInNonTestMethods() {}
    }

    static class public_modifier_example {

        @Test
        public void this_will_fail_because_of_the_public_modifier() {}
    }

    static class private_modifier_example {

        @Test
        private void this_will_fail_because_of_the_private_modifier() { }
    }

    static class protected_modifier_example {

        @Test
        protected void this_will_fail_because_of_the_protected_modifier() { }
    }
}
