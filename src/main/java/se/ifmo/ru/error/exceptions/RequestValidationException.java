package se.ifmo.ru.error.exceptions;

import lombok.Getter;
import se.ifmo.ru.dto.response.ErrorResponse.FieldError;

import java.util.List;

@Getter
public class RequestValidationException extends RuntimeException {
    private final List<FieldError> errors;
    public RequestValidationException(String message) {
        super(message);
        this.errors = List.of();
    }
    public RequestValidationException(String field, String message) {
        super("Неправильные параметры");
        this.errors = List.of(new FieldError(field, message));
    }
}
