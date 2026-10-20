package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Date;

public record PersonCreateDto (@NotNull Date birthday, @NotNull @Positive Integer weight,
                               @NotNull @Valid LocationDto location) {
}
