package com.github.havarunner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrencyControlTest {

    @Test
    public void defaultsToAvailableProcessors() {
        assertEquals(Runtime.getRuntime().availableProcessors(), ConcurrencyControl.permits());
    }

    @Test
    public void defaultCanbeOverridenBySystemProperty() {
        try {
            System.setProperty("havarunner.maximum_parallelism", "99999");
            assertEquals(99999, ConcurrencyControl.permits());
        } finally {
            System.clearProperty("havarunner.maximum_parallelism");
        }
    }

}
