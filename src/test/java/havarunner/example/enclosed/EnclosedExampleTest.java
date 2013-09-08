package havarunner.example.enclosed;

import havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
public class EnclosedExampleTest {

    static class enclosed_example {

        @Test
        void using_static_classes_within_the_test_class_is_a_way_to_group_tests() {
        }
    }
}
