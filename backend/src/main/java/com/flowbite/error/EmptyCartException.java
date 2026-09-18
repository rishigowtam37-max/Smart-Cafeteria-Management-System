package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when checkout is attempted with nothing in the cart.
 *
 * <p>Replaces {@code System.out.println("Your cart is empty.")} in the original
 * {@code OrderService.placeOrder}, which returned {@code null} afterwards.
 */
public class EmptyCartException extends CafeteriaException {

    public EmptyCartException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
