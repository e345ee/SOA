package se.ifmo.ru.error.exceptions;

import jakarta.ws.rs.core.Response;

public class ResourceConflictException extends ApplicationException {
    public ResourceConflictException(String message) {
        super(Response.Status.CONFLICT, message, null);
    }
}
