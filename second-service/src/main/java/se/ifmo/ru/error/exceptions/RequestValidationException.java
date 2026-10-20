package se.ifmo.ru.error.exceptions;

public class RequestValidationException extends RuntimeException {
    public RequestValidationException(String field, String message) {
        super(field + ": " + message);
    }
}
