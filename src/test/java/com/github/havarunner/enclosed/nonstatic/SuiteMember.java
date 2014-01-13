package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

@PartOf(Suite.class)
class SuiteMember {
    private final String suiteObject;

    SuiteMember(String suiteObject) {
        this.suiteObject = suiteObject;
    }

    @Test
    public void outerTest() {
        WhenSuiteTest.outerFromSuiteCalled += 1;
    }

    class InnerSuiteClass {

        @Test
        public void innerTest() {
            WhenSuiteTest.innerFromSuiteCalled += 1;
        }
    }
}
