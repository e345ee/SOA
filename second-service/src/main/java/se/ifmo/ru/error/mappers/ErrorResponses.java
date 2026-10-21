package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.ifmo.ru.dto.response.ErrorResponse;

public final class ErrorResponses {
    public static Response build(Response.StatusType status, String detail) {
        String title = status.getReasonPhrase();
        if (title == null || title.isBlank())
            title = "HTTP " + status.getStatusCode();
        if (detail == null || detail.isBlank())
            detail = title;
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(title, status.getStatusCode(), detail))
                .build();
    }
}
