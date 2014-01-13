package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import org.junit.Test;

import java.util.Collection;

import static com.github.havarunner.TestHelper.run;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.synchronizedSet;
import static org.junit.Assert.assertEquals;

public class SameInstanceTest {

    final static Collection seenParentInstances = synchronizedSet(newHashSet());
    final static Collection seenChildInstances = synchronizedSet(newHashSet());
    final static Collection seenGrandChildInstances = synchronizedSet(newHashSet());

    @Test
    public void inner_class_test_cases_should_share_the_same_Java_instance() {
        run(new HavaRunner(SameInstance.class));
        assertEquals(1, seenParentInstances.size());
        assertEquals(1, seenChildInstances.size());
        assertEquals(1, seenGrandChildInstances.size());
    }
}
