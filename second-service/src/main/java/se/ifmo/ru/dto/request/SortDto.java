package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SortDto(@NotNull String field, @NotNull @Pattern(regexp = "ASC|DESC") String direction) {}
