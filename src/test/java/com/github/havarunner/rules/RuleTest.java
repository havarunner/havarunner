package com.github.havarunner.rules;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleTest {
    static Collection<Description> firstRuleApplications= Collections.synchronizedList(Lists.<Description>newArrayList());
    static Collection<Description> secondRuleApplications = Collections.synchronizedList(Lists.<Description>newArrayList());
    static boolean firstTestRun;
    static boolean secondTestRun;

    @Test
    public void HavaRunner_supports_JUnit_rules() {
        run(new HavaRunner(TestWithTwoRules.class));
        verify(firstRuleApplications);
        verify(secondRuleApplications);
        assertTrue(firstTestRun);
        assertTrue(secondTestRun);
    }

    private void verify(Collection<Description> ruleApplications) {
        assertEquals(2, ruleApplications.size());
        HavaRunner_applies_each_rule_on_each_test_method(ruleApplications);
    }

    private void HavaRunner_applies_each_rule_on_each_test_method(Collection<Description> ruleApplications) {
        List<String> calledTestMethods = Lists.newArrayList();
        for (Description ruleApplication : ruleApplications) {
            calledTestMethods.add(ruleApplication.getMethodName());
        }
        assertTrue(calledTestMethods.contains("hello"));
        assertTrue(calledTestMethods.contains("helloAgain"));
    }

    static class TestWithTwoRules {
        @Rule
        TestRule first = new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                firstRuleApplications.add(description);
                return base;
            }
        };

        @Rule
        TestRule second = new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                secondRuleApplications.add(description);
                return base;
            }
        };

        @Test
        void hello() {
            firstTestRun = true;
        }

        @Test
        void helloAgain() {
            secondTestRun = true;
        }
    }
}
