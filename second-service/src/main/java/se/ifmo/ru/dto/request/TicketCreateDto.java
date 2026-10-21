package se.ifmo.ru.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.ifmo.ru.enums.TicketType;
import se.ifmo.ru.validation.FiniteNumber;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateDto {
    @NotNull @Size(min = 1)
    private String name;

    @NotNull @Valid
    private CoordinatesDto coordinates;

    @NotNull @Positive @FiniteNumber
    private Float price;

    @JsonSetter(nulls = Nulls.SET)
    private TicketType type;

    @Positive
    private Long personId;

    @Valid
    private PersonInputDto person;

    @NotNull @Positive
    private Long eventId;

    @AssertTrue(message = "Specify exactly one of personId or person")
    @JsonIgnore
    public boolean isPersonSelectionValid() {
        return (personId != null) != (person != null);
    }
}
