package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SortDto(
        @NotBlank String field,
        @NotNull @Pattern(regexp = "(?i)ASC|DESC") String direction
) {
}
