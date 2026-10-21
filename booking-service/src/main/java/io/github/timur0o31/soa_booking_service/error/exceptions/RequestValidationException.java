package io.github.timur0o31.soa_booking_service.error.exceptions;

public class RequestValidationException extends RuntimeException {
    public RequestValidationException(String field, String message) {
        super(field + ": " + message);
    }
}
