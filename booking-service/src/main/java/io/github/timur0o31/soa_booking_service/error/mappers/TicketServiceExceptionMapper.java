package io.github.timur0o31.soa_booking_service.error.mappers;

import io.github.timur0o31.soa_booking_service.error.exceptions.TicketServiceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class TicketServiceExceptionMapper implements ExceptionMapper<TicketServiceException> {
    @Override
    public Response toResponse(TicketServiceException exception) {
        log.warn("Ticket service request failed", exception);
        return ErrorResponses.build(Response.Status.BAD_GATEWAY, exception.getMessage());
    }
}
