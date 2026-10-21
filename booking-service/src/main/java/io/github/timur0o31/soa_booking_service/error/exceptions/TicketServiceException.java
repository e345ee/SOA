package io.github.timur0o31.soa_booking_service.error.exceptions;

public class TicketServiceException extends RuntimeException {
    public TicketServiceException(String message) {
        super(message);
    }

    public TicketServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
