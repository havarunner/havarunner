package com.github.havarunner.sequentiality;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.allEqual;
import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ParallelismTest {

    static List<Integer> items = Collections.synchronizedList(Lists.<Integer>newArrayList());

    @BeforeClass
    public static void runTest() {
        run(new HavaRunner(ParallelTest.class));
    }

    @Test
    public void HavaRunner_runs_tests_in_parallel_by_default() {
        assertFalse(allEqual(1, items.subList(0, 100)));
    }

    static class ParallelTest {
        @Test
        void test1() throws InterruptedException {
            addToList(1);
        }

        @Test
        void test2() throws InterruptedException {
            addToList(2);
        }

        private void addToList(int num) throws InterruptedException {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(0, 50); // Sleep to increase chances of context switching
                items.add(num);
            }
        }
    }
}
