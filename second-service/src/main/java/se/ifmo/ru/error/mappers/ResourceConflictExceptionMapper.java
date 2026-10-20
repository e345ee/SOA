package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.error.exceptions.ResourceConflictException;

@Provider
public class ResourceConflictExceptionMapper implements ExceptionMapper<ResourceConflictException> {
    @Override
    public Response toResponse(ResourceConflictException exception) {
        return ErrorResponses.build(Response.Status.CONFLICT, exception.getMessage());
    }
}
