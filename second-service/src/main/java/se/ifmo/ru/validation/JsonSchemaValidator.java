package se.ifmo.ru.validation;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import se.ifmo.ru.error.exceptions.RequestValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

final class JsonSchemaValidator {
    private final JsonObject schemas;

    JsonSchemaValidator(JsonObject schemas) {
        this.schemas = schemas;
    }

    void validate(JsonValue value, JsonObject original, String path) {
        JsonObject schema = resolve(original);
        if (value == JsonValue.NULL) {
            if (schema.getBoolean("nullable", false)) {
                return;
            }
            throw invalid(path, "Null is not allowed; omit optional fields instead");
        }

        switch (schema.getString("type", "")) {
            case "object" -> validateObject(value, schema, path);
            case "array" -> validateArray(value, schema, path);
            case "string" -> validateString(value, schema, path);
            case "integer", "number" -> validateNumber(value, schema, path);
            case "boolean" -> {
                if (value != JsonValue.TRUE && value != JsonValue.FALSE) {
                    throw invalid(path, "Expected a boolean");
                }
            }
            default -> {
            }
        }

        JsonArray allowed = schema.getJsonArray("enum");
        if (allowed != null && !allowed.contains(value)) {
            throw invalid(path, "Value is not in the allowed enumeration");
        }
    }

    private JsonObject resolve(JsonObject schema) {
        if (!schema.containsKey("$ref")) {
            return schema;
        }
        String name = schema.getString("$ref").replaceAll("^.*/", "");
        return schemas.getJsonObject(name);
    }

    private void validateObject(JsonValue value, JsonObject schema, String path) {
        if (!(value instanceof JsonObject object)) {
            throw invalid(path, "Expected an object");
        }
        JsonObject properties = schema.getJsonObject("properties");
        JsonArray required = schema.getJsonArray("required");
        if (required != null) {
            for (JsonValue name : required) {
                String key = ((JsonString) name).getString();
                JsonObject property = properties == null ? null : properties.getJsonObject(key);
                if (!isReadOnly(property) && !object.containsKey(key)) {
                    throw invalid(path + "." + key, "Required field is missing");
                }
            }
        }
        if (properties != null) {
            for (String key : object.keySet()) {
                JsonObject property = properties.getJsonObject(key);
                if (property != null && !isReadOnly(property)) {
                    validate(object.get(key), property, path + "." + key);
                }
            }
        }
        validateOneOf(object, schema.getJsonArray("oneOf"), path);
    }

    private boolean isReadOnly(JsonObject property) {
        return property != null && resolve(property).getBoolean("readOnly", false);
    }

    private void validateOneOf(JsonObject object, JsonArray alternatives, String path) {
        if (alternatives == null) {
            return;
        }
        int matches = 0;
        for (JsonValue alternative : alternatives) {
            JsonArray required = ((JsonObject) alternative).getJsonArray("required");
            boolean matchesRequired = true;
            if (required != null) {
                for (JsonValue name : required) {
                    matchesRequired &= object.containsKey(((JsonString) name).getString());
                }
            }
            if (matchesRequired) {
                matches++;
            }
        }
        if (matches != 1) {
            throw invalid(path, "Specify exactly one of personId or person");
        }
    }

    private void validateArray(JsonValue value, JsonObject schema, String path) {
        if (!(value instanceof JsonArray array)) {
            throw invalid(path, "Expected an array");
        }
        for (int index = 0; index < array.size(); index++) {
            validate(array.get(index), schema.getJsonObject("items"), path + "[" + index + "]");
        }
    }

    private void validateString(JsonValue value, JsonObject schema, String path) {
        if (!(value instanceof JsonString text)) {
            throw invalid(path, "Expected a string");
        }
        String string = text.getString();
        if (string.codePointCount(0, string.length()) < schema.getInt("minLength", 0)) {
            throw invalid(path, "String is too short");
        }
        try {
            switch (schema.getString("format", "")) {
                case "date" -> {
                    if (!string.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")
                            || LocalDate.parse(string).getYear() < 1) {
                        throw new IllegalArgumentException();
                    }
                }
                case "date-time" -> {
                    if (!string.matches("[0-9]{4}-.*")
                            || OffsetDateTime.parse(string).getYear() < 1) {
                        throw new IllegalArgumentException();
                    }
                }
                default -> {
                }
            }
        } catch (RuntimeException exception) {
            throw invalid(path, "Invalid calendar date or RFC 3339 timestamp");
        }
    }

    private void validateNumber(JsonValue value, JsonObject schema, String path) {
        if (!(value instanceof JsonNumber number)) {
            throw invalid(path, "Expected a JSON number");
        }
        BigDecimal decimal = number.bigDecimalValue();
        String format = schema.getString("format", "");
        try {
            if (schema.getString("type").equals("integer")) {
                validateInteger(decimal, format);
            } else {
                validateFiniteNumber(decimal, format);
            }
        } catch (ArithmeticException exception) {
            throw invalid(path, "Number is outside the API type range");
        }
        if (schema.containsKey("minimum")) {
            int comparison = decimal.compareTo(schema.getJsonNumber("minimum").bigDecimalValue());
            if (comparison < 0 || (comparison == 0 && schema.getBoolean("exclusiveMinimum", false))) {
                throw invalid(path, "Number is below the allowed minimum");
            }
        }
        if (schema.containsKey("maximum")) {
            int comparison = decimal.compareTo(schema.getJsonNumber("maximum").bigDecimalValue());
            if (comparison > 0 || (comparison == 0 && schema.getBoolean("exclusiveMaximum", false))) {
                throw invalid(path, "Number is above the allowed maximum");
            }
        }
    }

    private void validateInteger(BigDecimal number, String format) {
        switch (format) {
            case "int32" -> number.intValueExact();
            case "int64" -> number.longValueExact();
            default -> {
                if (number.stripTrailingZeros().scale() > 0) {
                    throw new ArithmeticException();
                }
            }
        }
    }

    private void validateFiniteNumber(BigDecimal number, String format) {
        double decimal = number.doubleValue();
        if (!Double.isFinite(decimal) || (decimal == 0 && number.signum() != 0)) {
            throw new ArithmeticException();
        }
        if (format.equals("float")) {
            float smallDecimal = number.floatValue();
            if (!Float.isFinite(smallDecimal) || (smallDecimal == 0 && number.signum() != 0)) {
                throw new ArithmeticException();
            }
        }
    }

    private RequestValidationException invalid(String path, String detail) {
        return new RequestValidationException(path, detail);
    }
}
