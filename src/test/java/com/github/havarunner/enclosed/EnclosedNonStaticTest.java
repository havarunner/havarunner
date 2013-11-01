package com.github.havarunner.enclosed;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.exception.ContainsNonStaticInnerClassException;
import org.junit.Test;
import org.junit.runner.notification.Failure;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class EnclosedNonStaticTest {

    @Test
    public void HavaRunner_gives_a_helpful_error_if_the_class_contains_nonstatic_inner_classes() {
        Failure failure = runAndRecordFailure(new HavaRunner(TestClass.class));
        assertEquals(ContainsNonStaticInnerClassException.class, failure.getException().getClass());
        assertEquals(
            "The class com.github.havarunner.enclosed.EnclosedNonStaticTest$TestClass$InnerClass must be static (HavaRunner does not support non-static inner classes)",
            failure.getMessage()
        );
    }

    @Test
    public void HavaRunner_gives_a_helpful_error_if_a_suite_member_contains_nonstatic_inner_classes() {
        Failure failure = runAndRecordFailure(new HavaRunner(Suite.class));
        assertEquals(ContainsNonStaticInnerClassException.class, failure.getException().getClass());
        assertEquals(
            "The class com.github.havarunner.enclosed.EnclosedNonStaticTest$SuiteMember$InnerSuiteClass must be static (HavaRunner does not support non-static inner classes)",
            failure.getMessage()
        );
    }

    static class TestClass {
        @Test
        public void outerTest() { }

        class InnerClass {

            @Test
            public void innerTest() { }
        }
    }

    static class Suite implements HavaRunnerSuite<String> {
        @Override
        public String suiteObject() {
            return "foo";
        }

        @Override
        public void afterSuite() {
        }
    }

    @PartOf(Suite.class)
    static class SuiteMember {
        private final String suiteObject;

        SuiteMember(String suiteObject) {
            this.suiteObject = suiteObject;
        }

        @Test
        public void outerTest() { }

        class InnerSuiteClass {

            @Test
            public void innerTest() { }
        }
    }
}
