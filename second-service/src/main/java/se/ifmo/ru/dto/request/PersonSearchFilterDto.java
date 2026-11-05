package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record PersonSearchFilterDto (
    @Min(1) Long id,
    LocalDate birthday,
    LocalDate birthdayFrom,
    LocalDate birthdayTo,
    @Min(1) Integer weight,
    @Min(1) Integer weightFrom,
    @Min(1) Integer weightTo,
    Integer locationX,
    Integer locationY,
    String locationName
    ){
}
