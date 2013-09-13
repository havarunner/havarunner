package com.github.havarunner.example.scenario;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.annotation.Scenarios;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static junit.framework.Assert.assertNotNull;

@RunWith(HavaRunner.class)
public class LoginPageTest {

    final User user;
    final Object loginPage;

    LoginPageTest(User user) {
        this.user = user;
        loginPage = "here be html";
    }

    @Test
    void login_page_looks_the_same_for_all_users() {
        if (user == User.ADMIN) {
            assertNotNull(loginPage);
        } else if (user == User.ANONYMOUS) {
            assertNotNull(loginPage);
        } else {
            throw new IllegalArgumentException("Unrecognised user " + user);
        }
    }

    enum User {
        ADMIN,
        ANONYMOUS
    }

    @Scenarios
    static Collection<User> users() {
        Collection<User> users = new ArrayList<>();
        Collections.addAll(users, User.values());
        return users;
    }
}
