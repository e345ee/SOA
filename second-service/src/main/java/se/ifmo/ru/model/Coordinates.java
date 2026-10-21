package se.ifmo.ru.model;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Coordinates {
    @Column(nullable = false)
    @DecimalMin(value = "-495", inclusive = false)
    private Double x;
    @Min(-262)
    private Integer y;
}
