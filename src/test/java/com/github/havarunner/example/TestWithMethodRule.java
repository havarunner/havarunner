package com.github.havarunner.example;

import com.github.havarunner.HavaRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assert.fail;

@RunWith(HavaRunner.class)
public class TestWithMethodRule {

    @Rule
    IgnoreAllRule rule = new IgnoreAllRule();

    @Test
    public void ShouldPassWithMethodRuleOn() {
        fail("Should be ignored");
    }

    public class IgnoreAllRule implements MethodRule {
        @Override
        public Statement apply(Statement base, FrameworkMethod method, Object target) {
            return new IgnoreStatement( );
        }
    }

    private static class IgnoreStatement extends Statement {
        @Override
        public void evaluate() {
            Assume.assumeTrue( "Ignored by Method rule ", false );
        }
    }

}
