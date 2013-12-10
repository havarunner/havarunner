package com.github.havarunner;

import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class TestInAbstractSuperclass {
    static boolean parentTestRun;

    @Test
    public void HavaRunner_should_discover_tests_in_abstract_super_class() {
        run(new HavaRunner(Child.class));
        assertTrue(parentTestRun);
    }

    static abstract class Parent {
        @Test
        void parentTest() {
            parentTestRun = true;
        }
    }

    static class Child extends Parent {
    }
}
