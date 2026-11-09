package io.github.timur0o31.soa_booking_service.error.exceptions;

import jakarta.ws.rs.core.Response;

public class RequestValidationException extends ApplicationException {
    public RequestValidationException(String field, String message) {
        super(Response.Status.BAD_REQUEST, field + ": " + message, null);
    }
}
