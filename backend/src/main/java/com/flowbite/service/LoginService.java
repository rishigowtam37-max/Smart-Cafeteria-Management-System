package com.flowbite.service;

import com.flowbite.model.Admin;
import com.flowbite.model.Customer;
import com.flowbite.model.User;
import org.springframework.stereotype.Service;

/**
 * Sign-in. Unchanged from the console app apart from this annotation - the web
 * layer calls the same polymorphic {@link #login(String, String, String)} the
 * console menu did, and gets back the same {@link User} subclass.
 *
 * <p>The two accounts stay hard-coded in the constructor, as they always were.
 * This is a demo with no user registration and no password storage to speak of.
 */
@Service
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