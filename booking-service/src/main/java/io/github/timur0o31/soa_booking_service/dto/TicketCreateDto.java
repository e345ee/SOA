package io.github.timur0o31.soa_booking_service.dto;

import io.github.timur0o31.soa_booking_service.enums.TicketType;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateDto {
    private String name;
    private CoordinatesDto coordinates;
    private Float price;
    private TicketType type;
    private Long personId;
    private Long eventId;
}
