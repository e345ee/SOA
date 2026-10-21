package io.github.timur0o31.soa_booking_service.error.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.StringJoiner;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().stream()
                .map(violation -> fieldName(violation.getPropertyPath()) + ": " + violation.getMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        return ErrorResponses.build(Response.Status.BAD_REQUEST, detail);
    }

    private String fieldName(Path path) {
        StringJoiner field = new StringJoiner(".");
        for (Path.Node node : path) {
            if (node.getKind() == ElementKind.PROPERTY)
                field.add(node.getName());
        }
        return field.length() == 0 ? "$" : field.toString();
    }
}
