package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

@PartOf(SuiteWithMemberThatHasScenarios.class)
class SuiteMemberWithScenarios {
    final String suiteObject;
    final Integer scenario;

    SuiteMemberWithScenarios(String suiteObject, Integer scenario) {
        this.suiteObject = suiteObject;
        this.scenario = scenario;
    }

    @Test
    public void outerTest() {
        ScenariosAndSuiteTest.outerFromSuiteCalled.incrementAndGet();
    }

    @Scenarios
    static List<Integer> ints() {
        return Lists.newArrayList(1, 2);
    }

    class InnerSuiteClass {

        @Test
        public void innerTest() {
            ScenariosAndSuiteTest.seenScenariosInInnerClass.add(scenario);
            ScenariosAndSuiteTest.innerFromSuiteCalled.incrementAndGet();
        }
    }
}
