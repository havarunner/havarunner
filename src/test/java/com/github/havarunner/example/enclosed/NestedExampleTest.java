package com.github.havarunner.example.enclosed;

import com.github.havarunner.HavaRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
public class NestedExampleTest {

    static class parent {
        @Test
        void you_can_use_inner_classes_recursively() {
            System.out.println("hello nested example @ " + getClass().getSimpleName());
        }
        class child {
            @Test
            void you_can_use_inner_classes_recursively() {
                System.out.println("hello nested example @ " + getClass().getSimpleName());
            }
            class grandchild {
                @Test
                void you_can_use_inner_classes_recursively() {
                    System.out.println("hello nested example @ " + getClass().getSimpleName());
                }
                class grandgrandchild {
                    @Test
                    void you_can_use_inner_classes_recursively() {
                        System.out.println("hello nested example @ " + getClass().getSimpleName());
                    }
                }
            }
        }
    }
}
