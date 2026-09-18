package com.flowbite.web;

import com.flowbite.error.CafeteriaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns every refusal into the same small JSON shape:
 *
 * <pre>{"message": "Not enough stock available.", "status": 409}</pre>
 *
 * <p>The message is the one the domain threw - which is the line the console app
 * printed - so the browser can show it verbatim instead of inventing its own
 * wording for a rule it does not know about.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** The error body. */
    public record ApiError(String message, int status) {
    }

    /** Every rule the cafeteria enforces: stock, empty carts, unknown items, roles. */
    @ExceptionHandler(CafeteriaException.class)
    public ResponseEntity<ApiError> handleCafeteriaException(CafeteriaException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ApiError(e.getMessage(), e.getStatus().value()));
    }

    /** Bean validation on a request body - reports the first field message. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationFailure(MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("That request was not valid.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(message, HttpStatus.BAD_REQUEST.value()));
    }

    /** Malformed or missing JSON body. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("That request could not be read.", HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Anything unplanned. Logged in full on the server, reported vaguely to the
     * browser - a stack trace is not a customer's business.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {

        log.error("Unhandled error serving a request", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Something went wrong at the counter. Please try again.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
