package io.github.timur0o31.soa_booking_service.error.mappers;

import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RequestValidationExceptionMapper implements ExceptionMapper<RequestValidationException> {
    @Override
    public Response toResponse(RequestValidationException exception) {
        return ErrorResponses.build(Response.Status.BAD_REQUEST, exception.getMessage());
    }
}
