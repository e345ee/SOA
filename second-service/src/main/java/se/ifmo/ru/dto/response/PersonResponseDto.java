package se.ifmo.ru.dto.response;

import jakarta.validation.Valid;
import se.ifmo.ru.dto.request.LocationDto;

import java.util.Date;

public record PersonResponseDto (Long id, @Valid Date birthday, @Valid Integer weight,
                                 @Valid LocationDto location){
}
