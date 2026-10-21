package se.ifmo.ru.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import se.ifmo.ru.dto.request.CoordinatesDto;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.enums.TicketType;

import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;

public record TicketResponseDto (Long id, @Valid String name, @Valid CoordinatesDto coordinates, @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date creationDate,
                                 Float price, TicketType type, PersonResponseDto person, Long eventId) {
}
