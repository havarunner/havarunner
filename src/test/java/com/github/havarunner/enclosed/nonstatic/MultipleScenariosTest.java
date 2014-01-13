package com.github.havarunner.enclosed.nonstatic;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultipleScenariosTest {
    static final List<String> seenScenarios = Collections.synchronizedList(Lists.<String>newArrayList());

    @Test
    public void HavaRunner_allows_combining_of_scenarios_and_non_static_inner_classes() {
        run(new HavaRunner(NonStaticEnclosedAndScenarios.class));
        assertEquals("The inner test should be called once for each scenario", 2, seenScenarios.size());
        assertTrue(seenScenarios.contains("first"));
        assertTrue(seenScenarios.contains("second"));
    }
}
