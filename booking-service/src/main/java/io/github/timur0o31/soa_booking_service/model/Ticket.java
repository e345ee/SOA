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
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates;//Поле не может быть null
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Float price; //Поле может быть null, Значение поля должно быть больше 0
    private TicketType type; //Поле не может быть null
    private Person person; //Поле не может быть null
    private Long eventId;
}
