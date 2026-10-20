package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.dto.response.ErrorResponse;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOGGER = Logger.getLogger(WebApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        String message;
        if (status >= 500) {
            LOGGER.log(Level.SEVERE, "HTTP server error", exception);
            message = "Internal server error";
        } else {
            message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = exception.getResponse().getStatusInfo().getReasonPhrase();
            }
        }
        ErrorResponse error = new ErrorResponse(status, message, List.of());
        return Response.fromResponse(exception.getResponse())
                .header(HttpHeaders.CONTENT_LENGTH, null)
                .header(HttpHeaders.CONTENT_ENCODING, null)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }
}
