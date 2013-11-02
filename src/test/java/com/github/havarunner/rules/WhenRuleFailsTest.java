package com.github.havarunner.rules;

import com.github.havarunner.HavaRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.Statement;

import static com.github.havarunner.TestHelper.runAndRecordFailure;
import static org.junit.Assert.assertEquals;

public class WhenRuleFailsTest {

    @Test
    public void HavaRunner_will_give_a_helpful_error_message_if_a_rule_throws_an_exception() {
        Failure failure = runAndRecordFailure(new HavaRunner(TestWithRule.class));
        assertEquals("Fail!", failure.getMessage());
        assertEquals(IllegalStateException.class, failure.getException().getClass());
    }

    static class TestWithRule {
        @Rule
        TestRule rule = new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                throw new IllegalStateException("Fail!");
            }
        };

        @Test
        void hello() {
        }
    }
}
