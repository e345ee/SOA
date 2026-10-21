package se.ifmo.ru.validation;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.error.exceptions.RequestValidationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Provider
@Priority(Priorities.ENTITY_CODER - 100)
public final class ApiBodyValidation implements ContainerRequestFilter {
    private static final JsonObject SCHEMAS = loadSchemas();
    private static final JsonSchemaValidator VALIDATOR = new JsonSchemaValidator(SCHEMAS);

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String path = context.getUriInfo().getPath().replaceAll("^/+|/+$", "");
        String schema = schemaFor(context.getMethod(), path);
        if (schema == null) {
            return;
        }

        byte[] bytes = context.getEntityStream().readAllBytes();
        context.setEntityStream(new ByteArrayInputStream(bytes));
        VALIDATOR.validate(readBody(bytes), SCHEMAS.getJsonObject(schema), "$");
    }

    private static JsonObject loadSchemas() {
        try (InputStream input = ApiBodyValidation.class.getResourceAsStream("/api-schemas.json");
             JsonReader reader = Json.createReader(input)) {
            return reader.readObject();
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static JsonValue readBody(byte[] bytes) {
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
            return reader.readValue();
        } catch (RuntimeException exception) {
            throw new RequestValidationException("$", "Expected a valid JSON request body");
        }
    }

    private static String schemaFor(String method, String path) {
        if (method.equals("POST")) {
            return switch (path) {
                case "tickets" -> "TicketCreate";
                case "persons" -> "PersonCreate";
                case "tickets/search" -> "TicketSearchRequest";
                case "persons/search" -> "PersonSearchRequest";
                case "tickets/count-by-person" -> "Person";
                default -> null;
            };
        }
        if (method.equals("PUT")) {
            if (path.matches("tickets/[^/]+")) {
                return "Ticket";
            }
            if (path.matches("persons/[^/]+")) {
                return "Person";
            }
        }
        return null;
    }
}
