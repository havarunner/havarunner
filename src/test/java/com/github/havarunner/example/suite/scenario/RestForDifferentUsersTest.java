package com.github.havarunner.example.suite.scenario;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.PartOf;
import com.github.havarunner.annotation.Scenarios;
import com.github.havarunner.example.suite.WebApplicationSuiteTest;
import com.github.havarunner.example.suite.WebServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(HavaRunner.class)
@PartOf(WebApplicationSuiteTest.class)
public class RestForDifferentUsersTest {

    private final WebServer webServer;
    private final String user;

    public RestForDifferentUsersTest(WebServer webServer, String user) {
        this.webServer = webServer;
        this.user = user;
    }

    @Test
    void users_receive_personal_response() {
        assertEquals("<html><body><title>hello "+user+"</title></body></html>", webServer.htmlDocument("/", user));
    }

    @Scenarios
    static ArrayList<String> users() {
        ArrayList<String> users = new ArrayList<>();
        users.add("Kalevi");
        users.add("Pasi");
        return users;
    }
}
