package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a cart asks for more units than the counter has left.
 *
 * <p>Replaces {@code System.out.println("Not enough stock available.")} in the
 * original {@code CartService.addToCart}. 409 rather than 400: the request was
 * well formed, it just conflicts with the current state of the stock.
 */
public class InsufficientStockException extends CafeteriaException {

    public InsufficientStockException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
