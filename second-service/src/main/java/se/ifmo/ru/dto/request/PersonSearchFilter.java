package se.ifmo.ru.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import se.ifmo.ru.utils.SearchValidation;
import se.ifmo.ru.validation.CalendarDate;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PersonSearchFilter implements SearchFilter {
    @Positive
    private Long id;

    @CalendarDate
    private LocalDate birthday;

    @CalendarDate
    private LocalDate birthdayFrom;

    @CalendarDate
    private LocalDate birthdayTo;

    @Positive
    private Integer weight;

    @Positive
    private Integer weightFrom;

    @Positive
    private Integer weightTo;

    private Integer locationX;

    private Integer locationY;

    private String locationNameContains;

    @AssertTrue(message = "Range start must not exceed its end")
    @JsonIgnore
    public boolean isRangeValid() {
        return SearchValidation.ordered(birthdayFrom, birthdayTo)
                && SearchValidation.ordered(weightFrom, weightTo);
    }

    public Map<String, Object> toCriteria() {
        Map<String, Object> criteria = new LinkedHashMap<>();
        if (id != null) criteria.put("id", id);
        if (birthday != null) criteria.put("birthday", birthday);
        if (birthdayFrom != null) criteria.put("birthday>=", birthdayFrom);
        if (birthdayTo != null) criteria.put("birthday<=", birthdayTo);
        if (weight != null) criteria.put("weight", weight);
        if (weightFrom != null) criteria.put("weight>=", weightFrom);
        if (weightTo != null) criteria.put("weight<=", weightTo);
        if (locationX != null) criteria.put("location.x", locationX);
        if (locationY != null) criteria.put("location.y", locationY);
        if (locationNameContains != null) criteria.put("location.name~", locationNameContains);
        return criteria;
    }
}
