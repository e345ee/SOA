package se.ifmo.ru.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.*;
import lombok.Data;
import se.ifmo.ru.enums.TicketType;
import se.ifmo.ru.utils.SearchValidation;
import se.ifmo.ru.validation.CalendarDate;
import se.ifmo.ru.validation.FiniteNumber;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class TicketSearchFilter implements SearchFilter {
    @Positive
    private Long id;

    @Size(min = 1)
    private String nameContains;

    @DecimalMin(value = "-495", inclusive = false) @FiniteNumber
    private Double coordinatesXFrom;

    @FiniteNumber
    private Double coordinatesXTo;

    @Min(-262)
    private Integer coordinatesYFrom;

    private Integer coordinatesYTo;

    @CalendarDate
    private OffsetDateTime creationDateFrom;

    @CalendarDate
    private OffsetDateTime creationDateTo;

    @Positive @FiniteNumber
    private Float priceFrom;

    @Positive @FiniteNumber
    private Float priceTo;

    private TicketType type;

    @CalendarDate
    private LocalDate personBirthday;

    @CalendarDate
    private LocalDate personBirthdayFrom;

    @CalendarDate
    private LocalDate personBirthdayTo;

    @Positive
    private Integer personWeight;

    @Positive
    private Integer personWeightFrom;

    @Positive
    private Integer personWeightTo;

    private Integer personLocationX;

    private Integer personLocationY;

    private String personLocationNameContains;

    @Positive
    private Long eventId;

    @JsonIgnore
    private boolean typeSpecified;

    @JsonSetter(nulls = Nulls.SET)
    public void setType(TicketType type) {
        this.type = type;
        this.typeSpecified = true;
    }

    @AssertTrue(message = "Range start must not exceed its end")
    @JsonIgnore
    public boolean isRangeValid() {
        return SearchValidation.ordered(coordinatesXFrom, coordinatesXTo)
                && SearchValidation.ordered(coordinatesYFrom, coordinatesYTo)
                && SearchValidation.ordered(creationDateFrom, creationDateTo)
                && SearchValidation.ordered(priceFrom, priceTo)
                && SearchValidation.ordered(personBirthdayFrom, personBirthdayTo)
                && SearchValidation.ordered(personWeightFrom, personWeightTo);
    }

    public Map<String, Object> toCriteria() {
        Map<String, Object> criteria = new LinkedHashMap<>();
        if (id != null) criteria.put("id", id);
        if (nameContains != null) criteria.put("name~", nameContains);
        if (coordinatesXFrom != null) criteria.put("coordinates.x>=", coordinatesXFrom);
        if (coordinatesXTo != null) criteria.put("coordinates.x<=", coordinatesXTo);
        if (coordinatesYFrom != null) criteria.put("coordinates.y>=", coordinatesYFrom);
        if (coordinatesYTo != null) criteria.put("coordinates.y<=", coordinatesYTo);
        if (creationDateFrom != null) criteria.put("creationDate>=", Date.from(creationDateFrom.toInstant()));
        if (creationDateTo != null) criteria.put("creationDate<=", Date.from(creationDateTo.toInstant()));
        if (priceFrom != null) criteria.put("price>=", priceFrom);
        if (priceTo != null) criteria.put("price<=", priceTo);
        if (typeSpecified) {
            criteria.put("type", type);
        }
        if (personBirthday != null) criteria.put("person.birthday", personBirthday);
        if (personBirthdayFrom != null) criteria.put("person.birthday>=", personBirthdayFrom);
        if (personBirthdayTo != null) criteria.put("person.birthday<=", personBirthdayTo);
        if (personWeight != null) criteria.put("person.weight", personWeight);
        if (personWeightFrom != null) criteria.put("person.weight>=", personWeightFrom);
        if (personWeightTo != null) criteria.put("person.weight<=", personWeightTo);
        if (personLocationX != null) criteria.put("person.location.x", personLocationX);
        if (personLocationY != null) criteria.put("person.location.y", personLocationY);
        if (personLocationNameContains != null) criteria.put("person.location.name~", personLocationNameContains);
        if (eventId != null) criteria.put("eventId", eventId);
        return criteria;
    }
}
