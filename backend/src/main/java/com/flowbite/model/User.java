package com.flowbite.model;

import com.flowbite.interfaces.Authenticatable;

public abstract class User implements Authenticatable {

    private String username;
    private String password;
    private String name;

    // Constructor
    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Common login method
    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) &&
               this.password.equals(password);
    }
}