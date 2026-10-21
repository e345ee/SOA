package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.dto.request.TicketSearchFilter;
import se.ifmo.ru.error.exceptions.RequestValidationException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TicketSearchValidator {
    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "name", "coordinates.x", "coordinates.y", "creationDate", "price", "type",
            "person.birthday", "person.weight", "person.location.x", "person.location.y",
            "person.location.name", "eventId"
    );

    private TicketSearchValidator() {
    }

    public static Map<String, Object> validateSearch(SearchDto<TicketSearchFilter> request) {
        validateSorting(request.getSort());
        TicketSearchFilter filter = request.getFilter();
        if (filter == null) return Map.of();
        validateDigit(filter.coordinatesXFrom(), "coordinatesXFrom");
        validateDigit(filter.coordinatesXTo(), "coordinatesXTo");
        validateDigit(filter.priceFrom(), "priceFrom");
        validateDigit(filter.priceTo(), "priceTo");
        validateRange(filter.coordinatesXFrom(), filter.coordinatesXTo(), "coordinatesX");
        validateRange(filter.coordinatesYFrom(), filter.coordinatesYTo(), "coordinatesY");
        validateRange(filter.priceFrom(), filter.priceTo(), "price");
        validateRange(filter.personBirthdayFrom(), filter.personBirthdayTo(), "personBirthday");
        validateRange(filter.personWeightFrom(), filter.personWeightTo(), "personWeight");
        if (filter.creationDateFrom() != null && filter.creationDateTo() != null
                && filter.creationDateFrom().isAfter(filter.creationDateTo())) {
            throw new RequestValidationException("filter.creationDate", "Начало периода не должно быть позже конца");
        }
        return filter.toCriteria();
    }

    private static void validateSorting(List<SortDto> sorts) {
        if (sorts == null) return;
        for (SortDto sort : sorts) {
            if (sort == null) {
                throw new RequestValidationException("sort", "Укажите поле и направление сортировки");
            }
            String field = sort.field();
            if (field == null || !SORT_FIELDS.contains(field)) {
                throw new RequestValidationException("sort.field", "Недопустимое поле сортировки: " + field);
            }
            String direction = sort.direction();
            if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
                throw new RequestValidationException("sort.direction", "Направление должно быть ASC или DESC");
            }
        }
    }

    private static <T extends Comparable<? super T>> void validateRange(T from, T to, String field) {
        if (from != null && to != null && from.compareTo(to) > 0) {
            throw new RequestValidationException("filter." + field, "Нижняя граница не должна превышать верхнюю");
        }
    }

    private static void validateDigit(Number value, String field) {
        if (value != null && !Double.isFinite(value.doubleValue())) {
            throw new RequestValidationException("filter." + field, "Значение должно быть конечным числом");
        }
    }
}
