package havarunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import havarunner.annotation.Scenarios;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static havarunner.TestHelper.run;
import static org.junit.Assert.*;

public class FilterTest {

    @Test
    public void HavaRunner_supports_the_JUnit_filter_API() {
        HavaRunner havaRunner = new HavaRunner(TestClass.class);
        havaRunner.filter(new Filter() {
            public boolean shouldRun(Description description) {
                throw new UnsupportedOperationException("HavaRunner should not call this method");
            }
            public String describe() {
                return String.format("Method HavaRunner_should_run_only_this_method(%s)", TestClass.class.getName());
            }
        });
        run(havaRunner);
        assertTrue(TestClass.filteredTestCalled);
        assertFalse(TestClass.rejectedTestCalled);
    }

    @Test
    public void HavaRunner_supports_the_JUnit_filter_API_even_when_the_test_contains_scenarios() {
        HavaRunner havaRunner = new HavaRunner(TestClassWithScenarios.class);
        havaRunner.filter(new Filter() {
            public boolean shouldRun(Description description) {
                throw new UnsupportedOperationException("HavaRunner should not call this method");
            }
            public String describe() {
                return String.format("Method HavaRunner_should_run_only_this_method(%s)", TestClassWithScenarios.class.getName());
            }
        });
        run(havaRunner);
        assertEquals(Sets.newHashSet("first scenario", "second scenario"), TestClassWithScenarios.filteredTestCalledForScenarios);
        assertEquals(Sets.newHashSet(), TestClassWithScenarios.rejectedTestCalledForScenarios);
    }

    static class TestClass {

        static boolean filteredTestCalled;
        static boolean rejectedTestCalled;

        @Test
        void HavaRunner_should_run_only_this_method() {
            filteredTestCalled = true;
        }

        @Test
        void HavaRunner_should_reject_this_method() {
            rejectedTestCalled = true;
        }
    }

    static class TestClassWithScenarios {
        static Set<String> filteredTestCalledForScenarios = Sets.newHashSet();
        static Set<String> rejectedTestCalledForScenarios = Sets.newHashSet();

        private final String scenario;

        TestClassWithScenarios(String scenario) {
            this.scenario = scenario;
        }

        @Test
        void HavaRunner_should_run_only_this_method() {
            filteredTestCalledForScenarios.add(scenario);
        }

        @Test
        void HavaRunner_should_reject_this_method() {
            rejectedTestCalledForScenarios.add(scenario);
        }

        @Scenarios
        static Collection<String> scenarios() {
            return Lists.newArrayList("first scenario", "second scenario");
        }
    }
}
