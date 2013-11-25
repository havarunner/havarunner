package com.github.havarunner.sequentiality;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertEquals;

public class SequentialityOrderTest {

    static List<String> tests = Lists.newArrayList();

    @Test
    public void HavaRunner_runs_sequential_tests_in_the_order_they_were_parsed() {
        run(new HavaRunner(TestClass.class));
        assertEquals(Lists.newArrayList("test1", "test2", "test3"), tests);
    }

    @RunSequentially
    static class TestClass {

        @Test
        void test1() {
            tests.add("test1");
        }

        @Test
        void test2() {
            tests.add("test2");
        }

        @Test
        void test3() {
            tests.add("test3");
        }
    }
}
