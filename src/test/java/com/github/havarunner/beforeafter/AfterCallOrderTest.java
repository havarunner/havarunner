package com.github.havarunner.beforeafter;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.After;
import org.junit.Test;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class AfterCallOrderTest {
    static long grandParentAfterCalled;
    static long parentAfterCalled;
    static long childAfterCalled;

    @Test
    public void HavaRunner_calls_the_descendant_afters_first() {
        run(new HavaRunner(Child.class));
        assertTrue(childAfterCalled < parentAfterCalled);
        assertTrue(parentAfterCalled < grandParentAfterCalled);
    }

    static class Child extends Parent {
        @After
        void child() throws InterruptedException {
            Thread.sleep(1);
            childAfterCalled = System.currentTimeMillis();
        }

        @Test
        void hello() {}
    }

    abstract static class Parent extends GrandParent {
        @After
        void parent() throws InterruptedException {
            Thread.sleep(1);
            parentAfterCalled = System.currentTimeMillis();
        }
    }

    @RunSequentially
    abstract static class GrandParent {
        @After
        void grandParent() throws InterruptedException {
            Thread.sleep(1);
            grandParentAfterCalled = System.currentTimeMillis();
        }
    }
}
