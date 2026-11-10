package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDto {
    @NotNull @DecimalMin(value = "-495", inclusive = false)
    @Digits(integer = 309, fraction = 3, message = "Допустимо не более 3 знаков после точки")
    private Double x;

    @Min(-262)
    private Integer y;

}
