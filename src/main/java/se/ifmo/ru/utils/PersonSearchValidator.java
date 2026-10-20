package se.ifmo.ru.utils;

import jakarta.enterprise.context.ApplicationScoped;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import se.ifmo.ru.dto.request.SearchDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PersonSearchValidator {
    private static List<String> FIELDS = Arrays.asList("id", "name", "weight", "x", "y", "birthdate");
    public static void validateSearch(SearchDto searchDto) {
        if (searchDto==null) {
            throw new RequestValidationException("$", "search parameter is null");
        }
        validatePagination(searchDto);
        validateFilter(searchDto);
        validateSorting(searchDto);
    }
    private static void validatePagination(SearchDto searchDto) {
        Long page =  searchDto.getPage();
        Long size =  searchDto.getSize();
        if (page == null || page <= 0 || page > 1_000_000)
            throw new RequestValidationException("page", "page number " + page + " is invalid");
        if (size == null || size <= 0 || size > 100)
            throw new RequestValidationException("size", "size number " + size + " is invalid");
    }
    private static void validateFilter(SearchDto searchDto){
        if (searchDto.getFilter() == null) return;
        for (var entry: searchDto.getFilter().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!FIELDS.contains(key)) {
                throw new RequestValidationException("filter", "filter key " + key + " is invalid");
            }
            switch(key){
                case "id" -> validateLong(value, key);
                case "x" -> validateInteger(value, key);
                case "name" -> validateString(value,key);
                case "weight" -> validateDouble(value, key);
                case "y" -> validateDouble(value, key);
                case "birthdate" -> validateDate(value, key);
            }
        }
    }
    private static void validateInteger(Object value, String field) {
        try {
            Integer.parseInt(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }
    private static void validateLong(Object value, String field) {
        try {
            Long.parseLong(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }
    private static void validateDouble(Object value, String field) {
        try {
            double number = Double.parseDouble(value.toString());
            if (!Double.isFinite(number)) {
                throw new RequestValidationException("filter." + field, "Invalid " + field);
            }
        } catch (NumberFormatException | NullPointerException e) {
            throw new RequestValidationException("filter." + field, "Invalid " + field);
        }
    }

    private static void validateDate(Object value, String field) {
        try {
            LocalDateTime.parse(value.toString());
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
            if (!FIELDS.contains(field)) {
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
