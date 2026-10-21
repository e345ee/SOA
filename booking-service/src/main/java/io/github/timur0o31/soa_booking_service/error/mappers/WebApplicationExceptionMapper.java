package io.github.timur0o31.soa_booking_service.error.mappers;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        String detail = exception.getMessage();
        if (original.getStatus() >= 500) {
            log.error("HTTP server error", exception);
            detail = "Внутренняя ошибка сервера";
        }
        Response error = ErrorResponses.build(original.getStatusInfo(), detail);
        return Response.fromResponse(original)
                .header(HttpHeaders.CONTENT_LENGTH, null)
                .header(HttpHeaders.CONTENT_ENCODING, null)
                .type(error.getMediaType())
                .entity(error.getEntity())
                .build();
    }
}
