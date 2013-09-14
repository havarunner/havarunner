package com.github.havarunner.example.suite.browser;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.example.suite.WebApplicationSuiteTest;
import com.github.havarunner.example.suite.WebServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(HavaRunner.class)
@PartOf(WebApplicationSuiteTest.class)
public class BrowserTest {

    private final WebServer webServer;

    BrowserTest(WebServer webServer) { // HavaRunner will pass the suite object to the constructor
        this.webServer = webServer;
    }

    @Test
    void the_front_page_contains_title() {
        assertTrue(webServer.htmlDocument("/").contains("<title>hello HawaRunner</title>"));
    }
}
