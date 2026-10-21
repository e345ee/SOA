package se.ifmo.ru.dto.response;

import jakarta.validation.Valid;
import se.ifmo.ru.dto.request.LocationDto;

import java.time.LocalDate;

public record PersonResponseDto (Long id, @Valid LocalDate birthday, @Valid Integer weight,
                                 @Valid LocationDto location){
}
