package com.github.havarunner.suite.suitedefinedinparent;

import org.junit.Test;

class SuiteMember extends AbstractSuiteMember {

    SuiteMember(String suiteObject) {
    }

    @Test
    public void hello() {
        SuiteDefinedInParentTest.suiteMethodCalled = true;
    }
}
