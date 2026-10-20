package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.ifmo.ru.dto.response.ErrorResponse;

import java.util.List;

public final class ErrorResponses {

    private ErrorResponses() {
    }
    public static Response build(Response.StatusType status, String message) {
        return build(status, message, List.of());
    }

    public static Response build(Response.StatusType status, String message, List<ErrorResponse.FieldError> errors) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(status.getStatusCode(), message, errors))
                .build();
    }
}
