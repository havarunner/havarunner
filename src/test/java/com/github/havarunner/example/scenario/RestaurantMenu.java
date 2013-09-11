package com.github.havarunner.example.scenario;

import static org.junit.Assert.assertNotNull;

class RestaurantMenu {
    final Person person;

    RestaurantMenu(Person person) {
        assertNotNull(person);
        this.person = person;
    }

    int price() {
        return person == Person.KID ? 5 : 20;
    }

    int courses() {
        return 4;
    }

    String suggestions() {
        return person == Person.KID ? "ice cream" : "coffee";
    }
}
