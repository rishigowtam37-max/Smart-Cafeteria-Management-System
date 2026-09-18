package com.flowbite.error;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a service FlowBite depends on - currently only the Gemini API -
 * fails or answers with something unusable.
 *
 * <p>502 rather than 500: nothing is wrong with this server, the request simply
 * could not be completed because something upstream would not cooperate. The
 * message is written for the customer, and the detail goes to the server log.
 */
public class UpstreamServiceException extends CafeteriaException {

    public UpstreamServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
