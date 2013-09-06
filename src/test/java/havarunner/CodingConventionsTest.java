package havarunner;


import havarunner.exception.CamelCasedException;
import havarunner.exception.MemberIsNotPackagePrivateException;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

public class CodingConventionsTest {

    @Test(expected = CamelCasedException.class)
    public void HavaRunner_fails_if_a_test_method_is_camel_cased() throws InitializationError {
        new HavaRunner(CamelCaseExamples.class).getChildren();
    }

    @Test
    public void HavaRunner_requires_snake_cased_methods() throws InitializationError {
        new HavaRunner(snake_cases_examples.class).getChildren();
    }

    @Test(expected = MemberIsNotPackagePrivateException.class)
    public void HavaRunner_rejects_public_test_methods() throws InitializationError {
        new HavaRunner(public_modifier_example.class).getChildren();
    }

    @Test(expected = MemberIsNotPackagePrivateException.class)
    public void HavaRunner_rejects_private_test_methods() throws InitializationError {
        new HavaRunner(private_modifier_example.class).getChildren();
    }

    @Test(expected = MemberIsNotPackagePrivateException.class)
    public void HavaRunner_rejects_protected_test_methods() throws InitializationError {
        new HavaRunner(protected_modifier_example.class).getChildren();
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
