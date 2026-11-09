package se.ifmo.ru.error.exceptions;

import jakarta.ws.rs.core.Response;

public class RequestValidationException extends ApplicationException {
    public RequestValidationException(String field, String message) {
        super(Response.Status.BAD_REQUEST, field + ": " + message, null);
    }
}
