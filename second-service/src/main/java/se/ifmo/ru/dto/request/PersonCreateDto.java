package se.ifmo.ru.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record PersonCreateDto(@PastOrPresent @NotNull LocalDate birthday,
                              @Positive Integer weight,
                              @NotNull @Valid LocationDto location) {}
