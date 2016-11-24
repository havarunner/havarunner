package com.github.havarunner.rules;

import com.github.havarunner.HavaRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.fail;

@RunWith(HavaRunner.class)
public class AnnotatedRuleTest {

    @Rule
    public SomeTestRule testRule = new SomeTestRule();

    @Test
    @RunOnlyWhenPropertyEnabled
    public void someTest() {
        fail("Should not run because 'some.property' is not 'MYSETTING'");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface RunOnlyWhenPropertyEnabled {}

    public static class SomeTestRule implements TestRule {
        public Statement apply(Statement base, Description description ) {
            if(description.getAnnotation(RunOnlyWhenPropertyEnabled.class) != null) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        Assume.assumeTrue(
                            "This test should be run only when we have 'some.property' defined as 'MYSETTING'",
                            System.getProperty("some.property").equals("MYSETTING")
                        );
                    }
                };
            }

            return base;
        }
    }
}
