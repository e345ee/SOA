package se.ifmo.ru.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record PersonInputDto(@Positive Long id, @NotNull LocalDate birthday, @Positive Integer weight, @NotNull @Valid LocationDto location) {
 public PersonCreateDto toCreate() { return new PersonCreateDto(birthday,weight,location); }
}
