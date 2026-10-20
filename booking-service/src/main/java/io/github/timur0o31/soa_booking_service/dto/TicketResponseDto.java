package io.github.timur0o31.soa_booking_service.dto;

import io.github.timur0o31.soa_booking_service.enums.TicketType;
import jakarta.validation.Valid;

import java.util.Date;

public record TicketResponseDto(Long id, @Valid String name, @Valid CoordinatesDto coordinates, Date creationDate,
                                Float price, TicketType ticketType, PersonResponseDto person, Long eventId) {
}
