package com.github.havarunner;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class InstanceGroupLockTest {

    @Test
    public void instance_group_lock_is_same_between_tests_of_same_class() {
        List<TestAndParameters> lists = Lists.newArrayList(new HavaRunner(TestClass.class).children());
        assertTrue(lists.get(0).instanceGroupLock() == lists.get(1).instanceGroupLock());
    }

    static class TestClass {
        @Test
        void first() { }

        @Test
        void second() { }
    }
}
