package io.github.timur0o31.soa_booking_service.error.exceptions;

import jakarta.ws.rs.core.Response;

public class TicketServiceException extends ApplicationException {
    public TicketServiceException(String message) {
        super(Response.Status.BAD_GATEWAY, message, null);
    }

    public TicketServiceException(String message, Throwable cause) {
        super(Response.Status.BAD_GATEWAY, message, cause);
    }
}
