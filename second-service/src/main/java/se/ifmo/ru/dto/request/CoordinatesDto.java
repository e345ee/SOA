package se.ifmo.ru.dto.request;
import jakarta.validation.constraints.*;
public record CoordinatesDto(@NotNull @DecimalMin(value="-495", inclusive=false) Double x, @Min(-262) Integer y) {}
