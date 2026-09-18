package com.flowbite.interfaces;

public interface Authenticatable {
    boolean login(String username, String password);
}