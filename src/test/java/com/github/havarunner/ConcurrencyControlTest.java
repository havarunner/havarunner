package com.github.havarunner;

import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;

public class ConcurrencyControlTest {


    @Test
    public void it_should_not_block_all_the_tests() {
        //assertEquals(1, ConcurrencyControl.adaptToLoadAndAcquire(new Semaphore(1), 3, 1));
    }

    @Test
    public void it_should_adapt_to_system_load() {
        //assertEquals(3, ConcurrencyControl.adaptToLoadAndAcquire(new Semaphore(32), 35, 32));
    }
}
