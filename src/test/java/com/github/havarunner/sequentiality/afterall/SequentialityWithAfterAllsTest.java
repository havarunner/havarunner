package com.github.havarunner.sequentiality.afterall;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.github.havarunner.TestHelper.assertAllEqual;
import static com.github.havarunner.TestHelper.run;

public class SequentialityWithAfterAllsTest {
    static List<Integer> ints = Lists.newArrayList();

    @Test
    public void HavaRunner_synchronises_the_AfterAll_methods() {
        run(new HavaRunner(SequentialSuite.class));
        if (ints.get(0) == 1) {
            assertAllEqual(1, ints.subList(0, 100));
            assertAllEqual(2, ints.subList(100, 200));
        } else {
            assertAllEqual(2, ints.subList(0, 100));
            assertAllEqual(1, ints.subList(100, 200));
        }
    }
}
