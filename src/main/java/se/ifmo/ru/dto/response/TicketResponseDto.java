package se.ifmo.ru.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import se.ifmo.ru.dto.request.CoordinatesDto;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.enums.TicketType;

import java.util.Date;

public record TicketResponseDto (Long id, @Valid String name, @Valid CoordinatesDto coordinates, Date creationDate,
                                 Float price, TicketType ticketType, PersonResponseDto person, Long eventId) {
}
