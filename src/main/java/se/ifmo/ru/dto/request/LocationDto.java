package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.NotNull;

public record LocationDto(@NotNull Integer x, @NotNull Integer y, @NotNull String name) {
}
