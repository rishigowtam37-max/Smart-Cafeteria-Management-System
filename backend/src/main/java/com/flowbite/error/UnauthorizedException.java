package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request has no signed-in user, or presents bad credentials.
 *
 * <p>Replaces the console app's "Invalid Admin Credentials." print, and covers
 * the case it never had to think about: a request arriving with no session at all.
 */
public class UnauthorizedException extends CafeteriaException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
