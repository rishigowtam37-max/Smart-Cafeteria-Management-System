package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for every rule the cafeteria refuses to break.
 *
 * <p>The console app signalled a broken rule by printing a line and returning.
 * That works when there is exactly one user staring at one terminal, but a web
 * request has to be told what went wrong. Each subclass therefore carries both
 * the original message - unchanged, so the console prints what it always did -
 * and the HTTP status the web layer should answer with.
 *
 * <p>See {@code ApiExceptionHandler}, which turns these into JSON, and
 * {@code ConsoleApp}, which catches them and prints {@link #getMessage()}.
 */
public abstract class CafeteriaException extends RuntimeException {

    private final HttpStatus status;

    protected CafeteriaException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * @return the HTTP status this failure maps to.
     */
    public HttpStatus getStatus() {
        return status;
    }
}
