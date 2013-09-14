package com.github.havarunner.example.suite.rest;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.example.suite.WebApplicationSuiteTest;
import com.github.havarunner.example.suite.WebServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(HavaRunner.class)
@PartOf(WebApplicationSuiteTest.class)
public class RestTest {

    private final WebServer webServer;

    RestTest(WebServer webServer) { // HavaRunner will pass the suite object to the constructor
        this.webServer = webServer;
    }

    @Test
    void rest_api_responds_200() {
        assertEquals(200, webServer.httpStatus(""));
    }
}
