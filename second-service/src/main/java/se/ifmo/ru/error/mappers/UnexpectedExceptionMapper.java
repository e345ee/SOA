package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.dto.response.ErrorResponse;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = Logger.getLogger(UnexpectedExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.log(Level.SEVERE, "Unexpected request failure", exception);
        ErrorResponse error = new ErrorResponse(500, "Internal server error", List.of());
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }
}
