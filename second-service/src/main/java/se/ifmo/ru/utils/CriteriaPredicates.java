package se.ifmo.ru.utils;

import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

final class CriteriaPredicates {
    private CriteriaPredicates() {}

    static <T> void add(List<Predicate> result, T value, Function<T, Predicate> condition) {
        if (value != null) result.add(condition.apply(value));
    }

    static String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT).replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
