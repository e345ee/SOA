package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.ifmo.ru.validation.CalendarDate;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonInputDto {
    @Positive
    private Long id;

    @NotNull @CalendarDate
    private LocalDate birthday;

    @Positive
    private Integer weight;

    @NotNull @Valid
    private LocationDto location;

    public PersonCreateDto toCreate() {
        return new PersonCreateDto(birthday, weight, location);
    }
}
