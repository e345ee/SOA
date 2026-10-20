package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.dto.response.ErrorResponse;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;

import java.util.List;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(404, exception.getMessage(), List.of());
        return Response.status(404)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error)
                .build();
    }
}
