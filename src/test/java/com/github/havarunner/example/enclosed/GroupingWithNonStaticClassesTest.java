package com.github.havarunner.example.enclosed;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.RunSequentially;
import com.github.havarunner.annotation.Scenarios;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(HavaRunner.class)
@RunSequentially
public class GroupingWithNonStaticClassesTest {
    final String book;

    GroupingWithNonStaticClassesTest(String book) {
        this.book = book;
    }

    @Test
    public void outer() {
        System.out.println("outer: " + book);
    }

    class Hello {

        @Before
        public void before() {
            System.out.println("before: " + getClass().getSimpleName());
        }

        @Test
        public void inner() {
            System.out.println("inner: " + book);
        }

        class Hello2 {

            @Test
            public void inner2() {
                System.out.println("inner2: " + book);
            }

            class Hello3 {

                @Test
                public void inner3() {
                    System.out.println("inner3: " + book);
                }
            }
        }
    }

    @Scenarios
    static List<String> books() {
        return asList("Pandora's star", "Judas Unchained");
    }
}
