package io.github.timur0o31.soa_booking_service.dto;

import io.github.timur0o31.soa_booking_service.enums.TicketType;

import java.util.Date;

public record TicketResponseDto(Long id, String name, CoordinatesDto coordinates, Date creationDate,
                                Float price, TicketType type, PersonResponseDto person, Long eventId) {
}
