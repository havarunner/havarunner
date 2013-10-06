package com.github.havarunner.rules;

import com.github.havarunner.HavaRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleTest {

    static Description firstRuleDescription;
    static Description secondRuleDescription;
    static boolean testRun;

    @Test
    public void HavaRunner_supports_JUnit_rules() {
        run(new HavaRunner(TestWithTwoRules.class));
        assertEquals("hello", firstRuleDescription.getMethodName());
        assertEquals("hello", secondRuleDescription.getMethodName());
        assertTrue(testRun);
    }

    static class TestWithTwoRules {
        @Rule
        TestRule first = new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                firstRuleDescription = description;
                return base;
            }
        };

        @Rule
        TestRule second = new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                secondRuleDescription = description;
                return base;
            }
        };

        @Test
        void hello() {
            testRun = true;
        }
    }
}
