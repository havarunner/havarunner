package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.annotation.Scenarios;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

class NonStaticEnclosedAndScenarios {

    final String scenario;

    NonStaticEnclosedAndScenarios(String scenario) {
        this.scenario = scenario;
    }

    @Test
    void firstOuterTest() {
    }

    @Test
    void secondOuterTest() {
    }

    class InnerClass {

        @Test
        void innerTest() {
            MultipleScenariosTest.seenScenarios.add(scenario);
        }
    }

    @Scenarios
    static List<String> scenarios() {
        return newArrayList("first", "second");
    }
}
