package com.github.havarunner.sequentiality;

import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.Test;

@PartOf(SequentialSuite.class)
@RunSequentially(because = "this suite member has its own reason for sequentiality")
class SuiteMemberOverridingRunSequentially {
    SuiteMemberOverridingRunSequentially(String suiteObject) {}

    @Test
    void test() {}
}
