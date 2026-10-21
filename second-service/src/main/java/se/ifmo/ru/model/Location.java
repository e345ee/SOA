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
    private Integer x;
    @NotNull
    private Integer y;
    @NotNull
    private String name;

}
