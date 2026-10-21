package io.github.timur0o31.soa_booking_service.error.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        log.error("Unexpected request failure", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }
}
