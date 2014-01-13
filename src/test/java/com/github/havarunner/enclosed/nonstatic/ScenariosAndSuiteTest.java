package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.havarunner.TestHelper.run;
import static java.util.Collections.synchronizedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScenariosAndSuiteTest {
    static final AtomicInteger outerFromSuiteCalled = new AtomicInteger(0);
    static final AtomicInteger innerFromSuiteCalled = new AtomicInteger(0);
    static final List<Integer> seenScenariosInInnerClass = synchronizedList(Lists.<Integer>newArrayList());

    @Test
    public void non_static_inner_classes_should_work_with_suites_and_scenarios() {
        run(new HavaRunner(SuiteWithMemberThatHasScenarios.class));
        assertEquals(2, outerFromSuiteCalled.get());
        assertEquals(2, innerFromSuiteCalled.get());
        assertTrue(seenScenariosInInnerClass.contains(1));
        assertTrue(seenScenariosInInnerClass.contains(2));
    }
}
