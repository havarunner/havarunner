package com.github.havarunner.suite.covariant;

import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

@PartOf(Suite.class)
class SuiteMember1 {

    private SuiteObject obj;

    public SuiteMember1(SuiteObject obj){
        this.obj = obj;
    }

    @Test
    public void testMethod(){
        SuiteConstructorSubclassTest.suiteMemberTestInvoked = true;
    }
}
