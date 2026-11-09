package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import se.ifmo.ru.enums.TicketType;

public record TicketUpdateDto(@NotNull @Size(min = 1, max = 255, message = "Название билета должно содержать от {min} до {max} символов")
                              String name, @NotNull @Valid CoordinatesDto coordinates,
                              @NotNull @Positive Float price, TicketType type, @NotNull @Valid PersonInputDto person,
                              @NotNull @Positive Long eventId) {
}
