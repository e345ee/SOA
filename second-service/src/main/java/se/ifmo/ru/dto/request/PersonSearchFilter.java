package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public record PersonSearchFilter(@Positive Long id,
        LocalDate birthday,
        LocalDate birthdayFrom,
        LocalDate birthdayTo,
        @Positive Integer weight,
        @Positive Integer weightFrom,
        @Positive Integer weightTo,
        Integer locationX,
        Integer locationY,
        String locationNameContains) {
    public Map<String, Object> toCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        add(criteria, "id", id);
        add(criteria, "birthday", birthday);
        add(criteria, "birthday>=", birthdayFrom);
        add(criteria, "birthday<=", birthdayTo);
        add(criteria, "weight", weight);
        add(criteria, "weight>=", weightFrom);
        add(criteria, "weight<=", weightTo);
        add(criteria, "location.x", locationX);
        add(criteria, "location.y", locationY);
        add(criteria, "location.name~", locationNameContains);
        return criteria;
    }

    private static void add(Map<String, Object> criteria, String key, Object value) {
        if (value != null) criteria.put(key, value);
    }
}
