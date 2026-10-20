package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import se.ifmo.ru.enums.TicketType;

public record TicketCreateDto (@NotBlank String name, @NotNull @Valid CoordinatesDto coordinates,
                               @NotNull @Positive Float price, TicketType type, @Positive Long personId,
                               @Valid PersonCreateDto person, @NotNull Long eventId)
{}
