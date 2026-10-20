package se.ifmo.ru.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import se.ifmo.ru.enums.TicketType;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    @Column(nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Embedded
    @NotNull
    private Coordinates coordinates;//Поле не может быть null

    @NotNull
    @CreationTimestamp
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @Positive
    @NotNull
    private Float price; //Поле не может быть null, Значение поля должно быть больше 0

    @Enumerated(EnumType.STRING)
    private TicketType type; //Поле  может быть null

    @NotNull
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person; //Поле не может быть null

    @NotNull
    private Long eventId;
}
