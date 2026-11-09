package io.github.timur0o31.soa_booking_service.error.mappers;

import io.github.timur0o31.soa_booking_service.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public final class ErrorResponses {
    private ErrorResponses() {
    }

    public static Response build(Response.StatusType status, String detail) {
        String title = status.getReasonPhrase();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(title, status.getStatusCode(), detail))
                .build();
    }
}
