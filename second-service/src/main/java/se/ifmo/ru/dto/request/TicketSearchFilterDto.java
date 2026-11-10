package se.ifmo.ru.dto.request;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.validation.constraints.*;
import lombok.Data;
import se.ifmo.ru.enums.TicketType;
import se.ifmo.ru.validation.ValidTicketsSearchRanges;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@ValidTicketsSearchRanges
public class TicketSearchFilterDto {
    @Positive private Long id;
    @Size(min = 1) private String nameContains;
    @DecimalMin(value = "-495", inclusive = false)
    @Digits(integer = 309, fraction = 3, message = "Допустимо не более 3 знаков после точки")
    private BigDecimal coordinatesXFrom;
    @DecimalMin(value = "-495", inclusive = false)
    @Digits(integer = 309, fraction = 3, message = "Допустимо не более 3 знаков после точки")
    private BigDecimal coordinatesXTo;
    @Min(-262) private Integer coordinatesYFrom;
    @Min(-262) private Integer coordinatesYTo;
    private OffsetDateTime creationDateFrom;
    private OffsetDateTime creationDateTo;
    @Positive private Float priceFrom;
    @Positive private Float priceTo;
    private TicketType type;
    private LocalDate personBirthday;
    private LocalDate personBirthdayFrom;
    private LocalDate personBirthdayTo;
    @Positive private Integer personWeight;
    @Positive private Integer personWeightFrom;
    @Positive private Integer personWeightTo;
    private Integer personLocationX;
    private Integer personLocationY;
    private String personLocationNameContains;
    @Positive private Long eventId;

    @JsonbTransient
    private boolean typeSpecified;

    public void setType(TicketType type) {
        this.type = type;
        this.typeSpecified = true;
    }
}
