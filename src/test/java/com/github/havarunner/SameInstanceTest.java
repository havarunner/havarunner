package com.github.havarunner;

import com.github.havarunner.HavaRunner;
import com.google.common.collect.Lists;
import com.github.havarunner.annotation.Scenarios;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.havarunner.TestHelper.run;
import static org.junit.Assert.assertSame;

@RunWith(Enclosed.class)
public class SameInstanceTest {

    public static class when_the_test_has_multiple_scenarios {

        static List<Object> instanceWhenMars = Lists.newArrayList();
        static List<Object> instanceWhenVenus = Lists.newArrayList();

        @Test
        public void HavaRunner_calls_each_test_method_on_the_same_instance() {
            run(new HavaRunner(TestWithScenarios.class));
            assertSame(instanceWhenMars.get(0), instanceWhenMars.get(1));
            assertSame(instanceWhenVenus.get(0), instanceWhenVenus.get(1));
        }

        static class TestWithScenarios {

            private final String planet;

            TestWithScenarios(String planet) {
                this.planet = planet;
            }

            @Test
            void test() {
                if (planet.equals("mars")) {
                    instanceWhenMars.add(this);
                } else if (planet.equals("venus")) {
                    instanceWhenVenus.add(this);
                } else {
                    throw new IllegalStateException("Unrecognised planed " + planet);
                }
            }

            @Test
            void another_test() {
                if (planet.equals("mars")) {
                    instanceWhenMars.add(this);
                } else if (planet.equals("venus")) {
                    instanceWhenVenus.add(this);
                } else {
                    throw new IllegalStateException("Unrecognised planed " + planet);
                }
            }

            @Scenarios
            static Collection<String> scenarios() {
                return Lists.newArrayList("mars", "venus");
            }
        }
    }

    public static class without_scenarios {
        static Object instanceWhenMilkyway;
        static Object instanceWhenAndromeda;

        @Test
        public void HavaRunner_calls_each_test_method_on_the_same_instance() {
            run(new HavaRunner(TestClass.class));
            assertSame(instanceWhenMilkyway, instanceWhenAndromeda);
        }

        static class TestClass {

            @Test
            void test() {
                instanceWhenAndromeda = this;
            }

            @Test
            void another_test() {
                instanceWhenMilkyway = this;
            }
        }
    }
}
