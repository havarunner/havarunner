package com.github.havarunner.rules;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MethodRuleTest {
    static Collection<FrameworkMethod> ruleApplications = Collections.synchronizedList(Lists.<FrameworkMethod>newArrayList());
    static boolean firstTestRun;
    static boolean secondTestRun;

    @Test
    public void HavaRunner_supports_JUnit_method_rules() {
        run(new HavaRunner(TestWithMethodRule.class));
        verify(ruleApplications);
        assertTrue(firstTestRun);
        assertTrue(secondTestRun);
    }

    private void verify(Collection<FrameworkMethod> ruleApplications) {
        assertEquals(2, ruleApplications.size());
        HavaRunner_applies_each_rule_on_each_test_method(ruleApplications);
    }

    private void HavaRunner_applies_each_rule_on_each_test_method(Collection<FrameworkMethod> ruleApplications) {
        List<String> calledTestMethods = Lists.newArrayList();
        for (FrameworkMethod frameworkMethod : ruleApplications) {
            calledTestMethods.add(frameworkMethod.getName());
        }
        assertTrue(calledTestMethods.contains("hello"));
        assertTrue(calledTestMethods.contains("helloAgain"));
    }

    static class TestWithMethodRule {
        @Rule
        MethodRule first = new MethodRule() {
            @Override
            public Statement apply(Statement base, FrameworkMethod method, Object target) {
                ruleApplications.add(method);
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
