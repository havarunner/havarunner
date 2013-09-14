package com.github.havarunner.suite;

import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

@PartOf(ExampleSuite.class)
class SuiteMember1 {

    SuiteMember1(String suiteObj) {
    }

    @Test
    void test() {
        RunningSuiteTest.when_running_the_whole_suite.suiteMember1Called = true;
    }
}
