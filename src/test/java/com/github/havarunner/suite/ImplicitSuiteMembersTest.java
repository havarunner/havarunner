package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.TestAndParameters;
import com.github.havarunner.annotation.PartOf;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;
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
        List<Class> expected = testClasses();
        assertEquals(sort(expected), sort(Lists.<Class>newArrayList(SuiteMember.class, SuiteMember.InnerSuiteClass.class)));
    }

    private Collection<Class> sort(List<Class> expected) {
        Collections.sort(expected, new Comparator<Class>() {
            @Override
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return expected;
    }

    private List<Class> testClasses() {
        List<Class>  classes = Lists.newArrayList();
        for (TestAndParameters testAndParameters : new HavaRunner(Suite.class).children()) {
            classes.add(testAndParameters.testClass());
        }
        return classes;
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
