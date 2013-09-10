package havarunner.example;

import havarunner.HavaRunner;
import havarunner.annotation.RunSequentially;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(HavaRunner.class)
@RunSequentially
public class SequantialExampleTest {

    @Test
    void this_test_is_run_in_the_main_thread() {
        assertEquals("main", Thread.currentThread().getName());
    }
}
