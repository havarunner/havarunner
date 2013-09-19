package com.github.havarunner.suite.sequential;

import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.Test;

@RunSequentially
@PartOf(Suite.class)
public class SequentialSuiteMember {

    public SequentialSuiteMember(String suiteObject) {

    }

    @Test
    public void test() {
        SequentialSuiteMembersTest.suiteMemberThread = Thread.currentThread();
    }
}
