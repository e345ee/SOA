package se.ifmo.ru.utils;

import se.ifmo.ru.error.exceptions.RequestValidationException;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.enums.TicketType;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TicketSearchValidator {
    private static final Set<String> FIELDS = Set.of("id", "name", "price", "type", "creationDate", "eventId");

    private TicketSearchValidator() {
    }

    public static Map<String, Object> validateSearch(SearchDto request) {
        if (request == null) {
            throw new RequestValidationException("$", "Search request is null");
        }
        validatePagination(request);
        Map<String, Object> filters = validateFilter(request);
        validateSorting(request);
        return filters;
    }

    private static void validatePagination(SearchDto request) {
        Long page = request.getPage();
        Long size = request.getSize();
        if (page == null || page <= 0 || page > 1_000_000) {
            throw new RequestValidationException("page", "Invalid page: " + page);
        }
        if (size == null || size <= 0 || size > 100) {
            throw new RequestValidationException("size", "Invalid size: " + size);
        }
    }

    private static Map<String, Object> validateFilter(SearchDto request) {
        Map<String, Object> filters = new HashMap<>();
        if (request.getFilter() == null) return filters;
        for (var entry : request.getFilter().entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (field == null || !FIELDS.contains(field)) {
                throw new RequestValidationException("filter", "Unknown filter: " + field);
            }
            if (value == null) {
                throw new RequestValidationException("filter." + field, "Filter " + field + " must not be null");
            }
            Object converted = switch (field) {
                case "id", "eventId" -> validateLong(value, field);
                case "name" -> validateString(value, field);
                case "price" -> validateFloat(value, field);
                case "type" -> validateType(value);
                case "creationDate" -> validateDate(value);
                default -> throw new RequestValidationException("filter", "Unknown filter: " + field);
            };
            filters.put(field, converted);
        }
        return filters;
    }

    private static void validateSorting(SearchDto request) {
        List<SortDto> sorts = request.getSort();
        if (sorts == null) return;
        for (int index = 0; index < sorts.size(); index++) {
            var sort = sorts.get(index);
            if (sort == null) {
                throw new RequestValidationException("sort[" + index + "]", "Invalid sort structure");
            }
            String field = sort.field();
            String direction = sort.direction();
            if (field == null || !FIELDS.contains(field)) {
                throw new RequestValidationException("sort[" + index + "].field", "Invalid sort field: " + field);
            }
            if (direction == null || !(direction.equalsIgnoreCase("ASC") ||
                    direction.equalsIgnoreCase("DESC"))) {
                throw new RequestValidationException("sort[" + index + "].direction", "Invalid sort direction");
            }
        }
    }

    private static Long validateLong(Object value, String field) {
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }

    private static Float validateFloat(Object value, String field) {
        try {
            float number = Float.parseFloat(value.toString());
            if (!Float.isFinite(number)) {
                throw new RequestValidationException("filter." + field, "Invalid " + field);
            }
            return number;
        } catch (NumberFormatException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }

    private static String validateString(Object value, String field) {
        if (!(value instanceof String str) || str.isBlank()) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
        return str;
    }

    private static TicketType validateType(Object value) {
        if (!(value instanceof String str)) {
            throw new RequestValidationException("filter.type", "Invalid ticket type");
        }
        try {
            return TicketType.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw new RequestValidationException("filter.type", "Invalid ticket type: " + str);
        }
    }

    private static Date validateDate(Object value) {
        try {
            return Date.from(Instant.parse(value.toString()));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new RequestValidationException("filter.creationDate", "Invalid creationDate");
        }
    }
}
