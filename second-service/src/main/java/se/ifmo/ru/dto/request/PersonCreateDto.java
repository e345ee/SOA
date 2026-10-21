package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PersonCreateDto (@NotNull LocalDate birthday, @NotNull @Positive Integer weight,
                               @NotNull @Valid LocationDto location) {
}
