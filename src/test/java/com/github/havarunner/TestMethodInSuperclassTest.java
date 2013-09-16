package com.github.havarunner;

import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class TestMethodInSuperclassTest {
    static boolean parentCalled;
    static boolean grandParentCalled;

    @Test
    public void HavaRunner_scans_for_test_methods_in_the_class_hierarchy() {
        run(new HavaRunner(Child.class));
        assertTrue(parentCalled);
        assertTrue(grandParentCalled);
    }


    static class Child extends Parent {

    }

    static class Parent extends GrandParent {
        @Test
        public void parentTest() {
            parentCalled = true;
        }
    }

    static class GrandParent {
        @Test
        public void grandParentTest() {
            grandParentCalled = true;
        }
    }
}
