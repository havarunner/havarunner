package com.github.havarunner.scenarios.duplicated;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(HavaRunner.class)
@PartOf(VerifyThatTestAreRunOnlyOnceSuiteImpl.class)
public class VerifyThatTestAreRunOnlyOnceTestOneImpl extends VerifyThatTestAreRunOnlyOnceTestSuperClass {
    VerifyThatTestAreRunOnlyOnceTestOneImpl(AtomicInteger suiteObject, List<String> scenarios) {
        super(suiteObject, scenarios);
    }


    @Scenarios
    static List<List<String>> scenarios() {
        List<String> strings = Lists.newArrayList("one", "two");
        List<String> strings2 = Lists.newArrayList("three", "four");
        List<String> strings3 = Lists.newArrayList("five", "six");
        return Lists.newArrayList(strings, strings2, strings3);
    }

}
