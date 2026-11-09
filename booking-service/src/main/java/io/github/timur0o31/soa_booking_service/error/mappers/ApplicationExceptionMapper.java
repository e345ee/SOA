package io.github.timur0o31.soa_booking_service.error.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.github.timur0o31.soa_booking_service.error.exceptions.ApplicationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {

    @Override
    public Response toResponse(ApplicationException exception) {
        if (exception.status().getStatusCode() >= 500) {
            log.error("Ошибка при обработке запроса", exception);
        }
        return ErrorResponses.build(exception.status(), exception.getMessage());
    }
}
