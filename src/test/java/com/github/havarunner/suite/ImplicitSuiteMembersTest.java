package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.annotation.PartOf;
import com.google.common.collect.Lists;
import org.junit.Test;

import static com.github.havarunner.TestHelper.assertTestClasses;
import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertTrue;

public class ImplicitSuiteMembersTest {

    static boolean implicitTestInvoked;

    @Test
    public void suites_implicitly_include_enclosed_classes_of_suite_members() {
        run(new HavaRunner(Suite.class));
        assertTrue(implicitTestInvoked);
    }

    @Test
    public void HavaRunner_includes_suite_members_exactly_once() {
        assertTestClasses(Lists.<Class>newArrayList(SuiteMember.class, SuiteMember.InnerSuiteClass.class), Suite.class);
    }

    static class Suite implements HavaRunnerSuite<String> {
        @Override
        public String suiteObject() {
            return "foo";
        }

        @Override
        public void afterSuite() {
        }
    }

    @PartOf(Suite.class)
    static class SuiteMember {
        private final String suiteObject;

        SuiteMember(String suiteObject) {
            this.suiteObject = suiteObject;
        }

        @Test
        public void outerTest() { }

        static class InnerSuiteClass {

            @Test
            public void innerTest() {
                implicitTestInvoked = true;
            }
        }
    }
}
