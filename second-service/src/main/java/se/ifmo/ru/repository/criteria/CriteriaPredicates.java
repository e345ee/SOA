package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class CriteriaPredicates {
    private CriteriaPredicates() {}

    static <T> void add(List<Predicate> result, T value, Function<T, Predicate> condition) {
        if (value != null) result.add(condition.apply(value));
    }

    static String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT).replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }

    public static Predicate equalOrNull(CriteriaBuilder cb, Path<?> path, Object value) {
        return value == null ? cb.isNull(path) : cb.equal(path, value);
    }
}
