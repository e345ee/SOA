package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.ifmo.ru.validation.FiniteNumber;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDto {
    @NotNull @DecimalMin(value = "-495", inclusive = false) @FiniteNumber
    private Double x;

    @Min(-262)
    private Integer y;

}
