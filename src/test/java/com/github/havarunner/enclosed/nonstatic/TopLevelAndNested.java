package com.github.havarunner.enclosed.nonstatic;

import org.junit.Test;

class TopLevelAndNested {
    @Test
    public void outerTest() {
        NonStaticTest.outerCalled += 1;
    }

    class InnerClass {

        @Test
        public void innerTest() {
            NonStaticTest.innerCalled += 1;
        }
    }
}
