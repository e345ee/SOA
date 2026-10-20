package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    private static final Logger LOGGER = Logger.getLogger(WebApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        String detail = exception.getMessage();
        if (original.getStatus() >= 500) {
            LOGGER.log(Level.SEVERE, "HTTP server error", exception);
            detail = "Internal server error";
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
