package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import se.ifmo.ru.enums.TicketType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public record TicketSearchFilter(
        @Positive Long id,
        @Size(min = 1) String nameContains,
        @DecimalMin(value = "-495", inclusive = false) Double coordinatesXFrom,
        Double coordinatesXTo,
        @Min(-262) Integer coordinatesYFrom,
        Integer coordinatesYTo,
        OffsetDateTime creationDateFrom,
        OffsetDateTime creationDateTo,
        @Positive Float priceFrom,
        @Positive Float priceTo,
        TicketType type,
        LocalDate personBirthday,
        LocalDate personBirthdayFrom,
        LocalDate personBirthdayTo,
        @Positive Integer personWeight,
        @Positive Integer personWeightFrom,
        @Positive Integer personWeightTo,
        Integer personLocationX,
        Integer personLocationY,
        String personLocationNameContains,
        @Positive Long eventId) {
    public Map<String, Object> toCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        add(criteria, "id", id);
        add(criteria, "name~", nameContains);
        add(criteria, "coordinates.x>=", coordinatesXFrom);
        add(criteria, "coordinates.x<=", coordinatesXTo);
        add(criteria, "coordinates.y>=", coordinatesYFrom);
        add(criteria, "coordinates.y<=", coordinatesYTo);
        add(criteria, "creationDate>=", toDate(creationDateFrom));
        add(criteria, "creationDate<=", toDate(creationDateTo));
        add(criteria, "price>=", priceFrom);
        add(criteria, "price<=", priceTo);
        add(criteria, "type", type);
        add(criteria, "person.birthday", personBirthday);
        add(criteria, "person.birthday>=", personBirthdayFrom);
        add(criteria, "person.birthday<=", personBirthdayTo);
        add(criteria, "person.weight", personWeight);
        add(criteria, "person.weight>=", personWeightFrom);
        add(criteria, "person.weight<=", personWeightTo);
        add(criteria, "person.location.x", personLocationX);
        add(criteria, "person.location.y", personLocationY);
        add(criteria, "person.location.name~", personLocationNameContains);
        add(criteria, "eventId", eventId);
        return criteria;
    }

    private static void add(Map<String, Object> criteria, String key, Object value) {
        if (value != null) criteria.put(key, value);
    }

    private static Date toDate(OffsetDateTime value) {
        return value == null ? null : Date.from(value.toInstant());
    }
}
