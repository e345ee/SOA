package se.ifmo.ru.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import se.ifmo.ru.enums.TicketType;
public record TicketCreateDto(@NotNull @Size(min=1) String name, @NotNull @Valid CoordinatesDto coordinates,
 @NotNull @Positive Float price, TicketType type, @Positive Long personId, @Valid PersonInputDto person, @NotNull @Positive Long eventId) {}
