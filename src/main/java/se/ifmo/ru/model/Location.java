package se.ifmo.ru.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {
    @NotNull
    private Integer x; // Поле не может быть null
    @NotNull
    private Integer y;// Поле не может быть null
    @NotNull
    private String name; //Поле может быть null

}
