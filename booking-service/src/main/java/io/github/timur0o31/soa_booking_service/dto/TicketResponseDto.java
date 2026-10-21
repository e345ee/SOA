package io.github.timur0o31.soa_booking_service.dto;

import io.github.timur0o31.soa_booking_service.enums.TicketType;
import jakarta.validation.Valid;

import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;

public record TicketResponseDto(Long id, @Valid String name, @Valid CoordinatesDto coordinates, @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date creationDate,
                                Float price, TicketType ticketType, PersonResponseDto person, Long eventId) {
}
