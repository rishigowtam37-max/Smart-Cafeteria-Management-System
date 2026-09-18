package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the request itself does not make sense - a quantity of zero, a
 * negative price, an empty name.
 *
 * <p>Replaces {@code System.out.println("Invalid quantity.")} and the other
 * input guards the console app printed inline.
 */
public class InvalidRequestException extends CafeteriaException {

    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
