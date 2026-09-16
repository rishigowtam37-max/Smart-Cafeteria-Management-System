package model;

public class Customer extends User {

    public Customer(String username, String password, String name) {
        super(username, password, name);
    }

    public void displayRole() {
        System.out.println("\n===== CUSTOMER PANEL =====");
        System.out.println("Welcome, " + getName() + "!");
    }
}