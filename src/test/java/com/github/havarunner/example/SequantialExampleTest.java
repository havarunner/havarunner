package com.github.havarunner.example;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
@RunSequentially
public class SequantialExampleTest {

    @Test
    void this_test_is_run_in_sequence_with_the_other_tests_in_this_class() {
    }

    @Test
    void this_test_is_also_run_in_sequence_with_the_other_tests_in_this_class() {
    }
}
