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
    private Long id;
    @Column(nullable = false)
    private String name;

    @Embedded
    @NotNull
    private Coordinates coordinates;

    @NotNull
    @CreationTimestamp
    private Date creationDate;

    @Positive
    @NotNull
    private Float price;

    @Enumerated(EnumType.STRING)
    private TicketType type;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @NotNull
    private Long eventId;
}
