package se.ifmo.ru.dto.response;

import jakarta.validation.Valid;
import se.ifmo.ru.dto.request.LocationDto;

import java.util.Date;

public record PersonResponseDto (Long id, @Valid Date birthDate, @Valid Double weight,
                                 @Valid LocationDto locationDto){
}
