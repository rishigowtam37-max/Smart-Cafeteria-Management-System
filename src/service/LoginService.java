package service;

import model.Admin;
import model.Customer;
import model.User;

public class LoginService {

    private Admin admin;
    private Customer customer;

    // Constructor
    public LoginService() {

        // Default Login Credentials

        admin = new Admin(
                "admin",
                "admin123",
                "Cafeteria Admin");

        customer = new Customer(
                "customer",
                "cust123",
                "Rishi");
    }

    // Admin Login
    public Admin adminLogin(String username, String password) {

        if (admin.login(username, password)) {
            return admin;
        }

        return null;
    }

    // Customer Login
    public Customer customerLogin(String username, String password) {

        if (customer.login(username, password)) {
            return customer;
        }

        return null;
    }

    // Generic Login (Polymorphism)
    public User login(String role, String username, String password) {

        if (role.equalsIgnoreCase("admin")) {
            return adminLogin(username, password);
        }

        if (role.equalsIgnoreCase("customer")) {
            return customerLogin(username, password);
        }

        return null;
    }
}