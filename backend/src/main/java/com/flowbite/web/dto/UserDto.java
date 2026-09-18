package com.flowbite.web.dto;

import com.flowbite.model.User;

/**
 * The signed-in user, minus the password.
 *
 * <p>{@link User} carries the password, so it must never be serialised to the
 * browser directly. This record is the reason that cannot happen by accident.
 */
public record UserDto(String role, String name, String username) {

    public static UserDto from(User user, String role) {
        return new UserDto(role, user.getName(), user.getUsername());
    }
}
