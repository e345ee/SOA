package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationDto(@NotNull Integer x, @NotNull Integer y,
                          @NotNull @Size(min = 1, max = 255, message = "Название локации должно содержать от {min} до {max} символов")
                          String name) {
}
