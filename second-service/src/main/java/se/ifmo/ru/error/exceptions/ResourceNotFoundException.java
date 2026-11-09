package se.ifmo.ru.error.exceptions;

import jakarta.ws.rs.core.Response;

public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String message) {
        super(Response.Status.NOT_FOUND, message, null);
    }
}
