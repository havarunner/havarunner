package com.github.havarunner.sequentiality.afterall;

import com.github.havarunner.annotation.AfterAll;
import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

import static com.github.havarunner.TestHelper.addHundredTimes;

@PartOf(SequentialSuite.class)
class SuiteMember1 {

    SuiteMember1(String suiteObject) {}

    @Test
    void test() {}

    @AfterAll
    void shutdown() throws InterruptedException {
        addHundredTimes(1, SequentialityWithAfterAllsTest.ints);
    }
}
