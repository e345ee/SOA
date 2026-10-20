package io.github.timur0o31.soa_booking_service.dto;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class TicketPageResponseDto {
    private List<Ticket> content;
    private Boolean hasNext;
}
