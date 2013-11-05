package com.github.havarunner.suite.sequential;

import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.Test;

@RunSequentially(because = "this test does not thrive in the concurrent world")
@PartOf(Suite.class)
class SequentialSuiteMember {

    public SequentialSuiteMember(String suiteObject) {

    }

    @Test
    public void test() {
    }
}
