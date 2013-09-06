package havarunner.example.scenario;

class RestaurantMenu {
    final Person person;

    RestaurantMenu(Person person) {
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
