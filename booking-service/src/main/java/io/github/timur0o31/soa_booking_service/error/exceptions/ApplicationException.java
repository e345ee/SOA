package io.github.timur0o31.soa_booking_service.error.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class ApplicationException extends RuntimeException {
    private final Response.Status status;

    protected ApplicationException(Response.Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Response.Status status() {
        return status;
    }
}
