package com.github.havarunner.scenarios.covariant;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.Scenarios;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collection;

import static com.github.havarunner.TestHelper.run;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static org.junit.Assert.assertEquals;

public class CovariantScenarioConstructorTest {
    static final Collection<Planet> seenPlanets = synchronizedList(Lists.<Planet>newArrayList());

    @Test
    public void havarunner_should_support_covariance_in_scenario_objects() {
        run(new HavaRunner(CovarianceInScenarioObject.class));
        assertEquals(2, seenPlanets.size());
    }

    static class CovarianceInScenarioObject {
        final Planet planet;

        CovarianceInScenarioObject(Planet planet) {
            this.planet = planet;
        }

        @Test
        public void all_planets_should_have_inhabitants() {
            seenPlanets.add(planet);
        }

        @Scenarios
        static Collection<Planet> planets() {
            return asList(
                new DysonAlpha(),
                new Planet() {
                    @Override
                    public String inhabitants() {
                        return "humans";
                    }
                }
            );
        }
    }

    interface Planet {
        String inhabitants();
    }

    static class DysonAlpha implements Planet {

        @Override
        public String inhabitants() {
            return "MorningLightMountain";
        }
    }
}
