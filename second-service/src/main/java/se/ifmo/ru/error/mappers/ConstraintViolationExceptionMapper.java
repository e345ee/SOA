package se.ifmo.ru.error.mappers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.dto.response.ErrorResponse;
import se.ifmo.ru.dto.response.ErrorResponse.FieldError;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<FieldError> errors = exception.getConstraintViolations()
                .stream()
                .map(violation ->
                        new FieldError(getFieldName(violation.getPropertyPath()),
                                violation.getMessage()))
                .toList();
        ErrorResponse error = new ErrorResponse(400, "Invalid request parameters", errors);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
    private String getFieldName(Path path) {
        String fieldName = "";
        for (Path.Node node : path) {
            if (node.getKind() == ElementKind.PROPERTY) {
                fieldName = node.getName();
            }
        }
        return fieldName.isEmpty() ? "$" : fieldName;
    }
}