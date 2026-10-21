package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.PersonSearchFilter;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.error.exceptions.RequestValidationException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PersonSearchValidator {
    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "birthday", "weight", "location.x", "location.y", "location.name"
    );

    private PersonSearchValidator() {
    }

    public static Map<String, Object> validateSearch(SearchDto<PersonSearchFilter> request) {
        validateSorting(request.getSort());
        PersonSearchFilter filter = request.getFilter();
        if (filter == null) return Map.of();
        validateRange(filter.birthdayFrom(), filter.birthdayTo(), "birthday");
        validateRange(filter.weightFrom(), filter.weightTo(), "weight");
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
        if (from != null && to != null && from.compareTo(to) > 0)
            throw new RequestValidationException("filter." + field, "Нижняя граница не должна превышать верхнюю");
    }
}
