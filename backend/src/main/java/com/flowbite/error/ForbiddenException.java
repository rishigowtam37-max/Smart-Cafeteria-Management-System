package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a signed-in user reaches for something their role does not cover -
 * a customer calling an admin endpoint, or an admin calling a cart endpoint.
 *
 * <p>The console app enforced this by only printing the menu for the role you
 * logged in as. A browser can send any request it likes, so the rule has to be
 * checked on arrival instead. See {@code RoleInterceptor}.
 */
public class ForbiddenException extends CafeteriaException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
