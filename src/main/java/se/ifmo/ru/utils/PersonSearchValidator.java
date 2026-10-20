package se.ifmo.ru.utils;

import jakarta.enterprise.context.ApplicationScoped;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import se.ifmo.ru.dto.request.SearchDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@ApplicationScoped
public class PersonSearchValidator {
    private static final Map<String, String> FIELDS = Map.of("id", "id",
            "weight", "weight",
            "birthdate", "birthDate",
            "x", "location.x",
            "y", "location.y");

    public static String field(String key) {
        return FIELDS.get(key);
    }
    public static Map<String,Object> validateSearch(SearchDto searchDto) {
        if (searchDto==null) {
            throw new RequestValidationException("$", "search parameter is null");
        }
        validatePagination(searchDto);
        Map<String, Object> filters = validateFilter(searchDto);
        validateSorting(searchDto);
        return filters;
    }
    private static void validatePagination(SearchDto searchDto) {
        Long page =  searchDto.getPage();
        Long size =  searchDto.getSize();
        if (page == null || page <= 0 || page > 1_000_000)
            throw new RequestValidationException("page", "page number " + page + " is invalid");
        if (size == null || size <= 0 || size > 100)
            throw new RequestValidationException("size", "size number " + size + " is invalid");
    }
    private static Map<String, Object> validateFilter(SearchDto searchDto){
        Map<String, Object> filters = new HashMap<>();
        if (searchDto.getFilter() == null) return filters;
        for (var entry: searchDto.getFilter().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!FIELDS.containsKey(key)) {
                throw new RequestValidationException("filter", "filter key " + key + " is invalid");
            }
            Object converted = switch (key) {
                case "id" -> validateLong(value, key);
                case "x", "y", "weight" -> validateInteger(value, key);
                case "birthdate" -> validateDate(value, key);
                default -> throw new RequestValidationException("filter", "filter key " + key + " is invalid");
            };
            filters.put(FIELDS.get(key), converted);
        }
        return filters;
    }
    private static Integer validateInteger(Object value, String field) {
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }
    private static Long validateLong(Object value, String field) {
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }
    private static Double validateDouble(Object value, String field) {
        try {
            double number = Double.parseDouble(value.toString());
            if (!Double.isFinite(number)) {
                throw new RequestValidationException("filter." + field, "Invalid " + field);
            }
            return number;
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }

    private static Date validateDate(Object value, String field) {
        try {
            return Date.from(Instant.parse(value.toString()));
        } catch (DateTimeParseException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }

    private static void validateString(Object value, String field) {
        if (!(value instanceof String str) || str.isBlank()) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }
    private static void validateSorting(SearchDto searchDto) {
        if (searchDto.getSort() == null) return;
        for (int index = 0; index < searchDto.getSort().size(); index++) {
            var sort = searchDto.getSort().get(index);
            if (sort == null ) {
                throw new RequestValidationException("sort[" + index + "]", "Invalid sort structure");
            }
            String field = sort.field();
            String direction = sort.direction();
            if (!FIELDS.containsKey(field)) {
                throw new RequestValidationException("sort[" + index + "].field", "Invalid sort field: " + field);
            }
            if (direction == null ||
                    !(direction.equalsIgnoreCase("ASC") ||
                            direction.equalsIgnoreCase("DESC"))) {
                throw new RequestValidationException("sort[" + index + "].direction", "Invalid sort direction");
            }
        }
    }
}
