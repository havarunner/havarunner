package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeforeCallOrderTest {
    static long grandParentBeforeCalled;
    static long parentBeforeCalled;
    static long childBeforeCalled;

    @Test
    public void HavaRunner_calls_the_ancestor_befores_first() {
        run(new HavaRunner(Child.class));
        assertTrue(grandParentBeforeCalled < parentBeforeCalled);
        assertTrue(parentBeforeCalled < childBeforeCalled);
    }

    static class Child extends Parent {
        @Before
        void childBefore() throws InterruptedException {
            Thread.sleep(1);
            childBeforeCalled = System.currentTimeMillis();
        }

        @Test
        void hello() {}
    }

    abstract static class Parent extends GrandParent {
        @Before
        void parentBefore() throws InterruptedException {
            Thread.sleep(1);
            parentBeforeCalled = System.currentTimeMillis();
        }
    }

    @RunSequentially
    abstract static class GrandParent {
        @Before
        void grandParentBefore() throws InterruptedException {
            Thread.sleep(1);
            grandParentBeforeCalled = System.currentTimeMillis();
        }
    }
}
