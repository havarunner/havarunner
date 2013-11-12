package com.github.havarunner.sequentiality;

import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

@PartOf(SequentialSuite.class)
class SuiteMember {
    SuiteMember(String suiteObject) {}

    @Test
    void test() {}
}
