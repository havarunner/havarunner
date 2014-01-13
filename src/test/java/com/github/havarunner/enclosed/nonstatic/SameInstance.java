package com.github.havarunner.enclosed.nonstatic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

class SameInstance {

    @Test
    public void outerTest() {
    }

    class Child {

        @Test
        public void childTest1() {
            SameInstanceTest.seenParentInstances.add(SameInstance.this);
            SameInstanceTest.seenChildInstances.add(this);
        }

        @Test
        public void childTest2() {
            SameInstanceTest.seenParentInstances.add(SameInstance.this);
            SameInstanceTest.seenChildInstances.add(this);
        }

        class GrandChild {

            @Test
            public void grandChildTest1() {
                SameInstanceTest.seenParentInstances.add(SameInstance.this);
                SameInstanceTest.seenChildInstances.add(Child.this);
                SameInstanceTest.seenGrandChildInstances.add(this);
            }

            @Test
            public void grandChildTest2() {
                SameInstanceTest.seenParentInstances.add(SameInstance.this);
                SameInstanceTest.seenChildInstances.add(Child.this);
                SameInstanceTest.seenGrandChildInstances.add(this);
            }
        }
    }
}
