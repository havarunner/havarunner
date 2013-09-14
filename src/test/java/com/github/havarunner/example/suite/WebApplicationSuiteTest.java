package com.github.havarunner.example.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import org.junit.runner.RunWith;

@RunWith(HavaRunner.class)
public class WebApplicationSuiteTest implements HavaRunnerSuite<WebServer> {

    final WebServer webServer;

    public WebApplicationSuiteTest() {
        this.webServer = new WebServer();
    }

    @Override
    public WebServer suiteObject() {
        return webServer;
    }

    @Override
    public void afterSuite() { // JVM calls this method in the shutdown hook
        webServer.shutDown();
    }
}
