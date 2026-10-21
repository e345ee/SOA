package io.github.timur0o31.soa_booking_service.model;

import io.github.timur0o31.soa_booking_service.enums.TicketType;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private Long id;
    private String name;
    private Coordinates coordinates;
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date creationDate;
    private Float price;
    private TicketType type;
    private Person person;
    private Long eventId;
}
