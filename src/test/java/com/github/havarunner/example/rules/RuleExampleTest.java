package com.github.havarunner.example.rules;

import com.github.havarunner.HavaRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

@RunWith(HavaRunner.class)
public class RuleExampleTest {
    @Rule
    TestRule greet = new TestRule() {
        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    System.out.println("hello");
                    base.evaluate();
                }
            };
        }
    };

    @Test
    public void HavaRunner_supports_JUnit_rules() {
        System.out.println("there");
    }
}
