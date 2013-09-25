package com.github.havarunner.suite.suitedefinedinparent;

import org.junit.Test;

class SuiteMember extends AbstractSuiteMember {

    SuiteMember(String suiteObject) {
    }

    @Test
    public void hello() {
        SuiteDefinedInParentTest.suiteMethodCalled = true;
    }

    static class InnerSuiteMember extends AbstractSuiteMember {
        InnerSuiteMember(String suiteObject) {

        }

        @Test
        public void hello_inner() {
            SuiteDefinedInParentTest.innerSuiteMethodCalled = true;
        }
    }
}
