package com.github.havarunner;

import com.github.havarunner.annotation.AfterAll;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.runAndIgnoreErrors;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

public class OneFailsOneSucceedsTest {

    static List<String> messages = Collections.synchronizedList(Lists.<String>newArrayList());

    @Test
    public void HavaRunner_calls_the_AfterAll_even_though_one_constructor_in_the_test_set_fails() { // This is a regression test
        runAndIgnoreErrors(new HavaRunner(OtherFails.class));
        assertEquals(1, messages.size());
        assertEquals("this one should succeed", messages.get(0));
    }

    static class OtherFails {

        final String param;

        OtherFails(String param) {
            this.param = param;
            if (param.equals("this one should fail")) {
                throw new RuntimeException("Fail on purpose");
            }
        }

        @Test
        void test() {
        }

        @AfterAll
        void finish() {
            messages.add(param);
        }

        @Scenarios
        static List<String> scenarios() {
            return newArrayList("this one should fail", "this one should succeed");
        }
    }
}
