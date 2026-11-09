package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.error.exceptions.ApplicationException;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {
    @Override
    public Response toResponse(ApplicationException exception) {
        return ErrorResponses.build(exception.status(), exception.getMessage());
    }
}
