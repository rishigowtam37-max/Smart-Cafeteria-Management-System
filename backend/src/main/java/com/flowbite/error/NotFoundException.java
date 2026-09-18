package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a food item or an order id does not exist.
 *
 * <p>Replaces {@code System.out.println("Food item not found.")}.
 */
public class NotFoundException extends CafeteriaException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
