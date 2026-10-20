package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.dto.response.ErrorResponse;
import se.ifmo.ru.error.exceptions.RequestValidationException;

@Provider
public class RequestValidationExceptionMapper implements ExceptionMapper<RequestValidationException> {
    @Override
    public Response toResponse(RequestValidationException exception) {
        ErrorResponse error = new ErrorResponse(400, exception.getMessage(), exception.getErrors());
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }
}
