package com.github.havarunner.sequentiality.afterall;

import com.github.havarunner.annotation.AfterAll;
import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

import static com.github.havarunner.TestHelper.addHundredTimes;

@PartOf(SequentialSuite.class)
class SuiteMember2 {

    SuiteMember2(String suiteObject) {}

    @Test
    void test() {}

    @AfterAll
    void shutdown() throws InterruptedException {
        addHundredTimes(2, SequentialityWithAfterAllsTest.ints);
    }
}
