package com.github.havarunner.scenarios;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.AfterAll;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AfterAllsWithScenariosTest {
    private static List<String> afterAllMethodsCalled = Lists.newArrayList();

    @Test
    public void HavaRunner_will_call_the_AfterAll_methods_for_all_the_scenario_instances() {
        run(new HavaRunner(ValidScenarioTest.class));
        assertEquals(afterAllMethodsCalled.size(), 2);
        assertTrue(afterAllMethodsCalled.contains("first"));
        assertTrue(afterAllMethodsCalled.contains("second"));
    }

    static class ValidScenarioTest {
        final String scenario;

        ValidScenarioTest(String scenario) {
            this.scenario = scenario;
        }

        @Scenarios
        static Set<String> scenarios() {
            return Sets.newHashSet("first", "second");
        }

        @Test
        void do_something_with_the_scenario() {
        }

        @AfterAll
        void after_all() {
            afterAllMethodsCalled.add(scenario);
        }
    }
}
