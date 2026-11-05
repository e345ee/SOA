package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import se.ifmo.ru.enums.TicketType;
import se.ifmo.ru.validation.ValidTicketsSearchRanges;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@ValidTicketsSearchRanges
public record TicketSearchFilterDto(
        @Positive Long id,
        String name,
        @Min(-495) BigDecimal coordinatesXFrom,
        @Min(-495) BigDecimal coordinatesXTo,
        @Min(-262) Integer coordinatesYFrom,
        @Min(-262) Integer coordinatesYTo,
        Instant creationDateFrom,
        Instant creationDateTo,
        @Min(0) Float priceFrom,
        @Min(0) Float priceTo,
        TicketType type,
        LocalDate personBirthday,
        LocalDate personBirthdayFrom,
        LocalDate personBirthdayTo,
        @Min(1) Integer personWeight,
        @Min(1) Integer personWeightFrom,
        @Min(1) Integer personWeightTo,
        Integer personLocationX,
        Integer personLocationY,
        String personLocationName,
        @Min(1)Long eventId

) {

}
