package se.ifmo.ru.dto.response;

import java.util.List;

public record ErrorResponse(int code, String message, List<FieldError> errors) {
    public record FieldError(String field, String message) {}
}