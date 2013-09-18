package com.github.havarunner.example.enclosed;

import com.github.havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
public class EnclosedExampleTest {

    static class enclosed_example {

        @Test
        void using_static_classes_within_the_test_class_is_a_way_to_group_tests() {
        }
    }

    static class parent {
        @Test
        void you_can_use_inner_classes_recursively() {
            System.out.println("hello recursive example @ " + getClass().getSimpleName());
        }
        static class child {
            @Test
            void you_can_use_inner_classes_recursively() {
                System.out.println("hello recursive example @ " + getClass().getSimpleName());
            }
            static class grandchild {
                @Test
                void you_can_use_inner_classes_recursively() {
                    System.out.println("hello recursive example @ " + getClass().getSimpleName());
                }
                static class grandgrandchild {
                    @Test
                    void you_can_use_inner_classes_recursively() {
                        System.out.println("hello recursive example @ " + getClass().getSimpleName());
                    }
                }
            }

        }
    }
}
