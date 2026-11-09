package se.ifmo.ru.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 255, message = "Название локации должно содержать от {min} до {max} символов")
    private String name;

}
