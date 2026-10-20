package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CoordinatesDto (@NotNull @DecimalMin(value = "-495", inclusive = false) Double x, @NotNull @Min(-262) Long y) {
}
