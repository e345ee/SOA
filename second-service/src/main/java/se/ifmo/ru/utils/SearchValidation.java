package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.util.List;
import java.util.Set;

public final class SearchValidation {
    private SearchValidation() {}

    public static <T extends Comparable<? super T>> boolean ordered(T from, T to) {
        return from == null || to == null || from.compareTo(to) <= 0;
    }

    public static void validateSorts(List<SortDto> sorts, Set<String> allowedFields) {
        if (sorts == null) return;
        for (SortDto sort : sorts) {
            if (sort == null || sort.field() == null || !allowedFields.contains(sort.field())) {
                throw new RequestValidationException("sort.field", "Unknown sort field");
            }
        }
    }
}
