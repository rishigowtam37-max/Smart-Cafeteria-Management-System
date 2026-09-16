package model;

public class Admin extends User {

    public Admin(String username, String password, String name) {
        super(username, password, name);
    }

    public void displayRole() {
        System.out.println("\n===== ADMIN PANEL =====");
        System.out.println("Welcome, " + getName() + "!");
    }
}