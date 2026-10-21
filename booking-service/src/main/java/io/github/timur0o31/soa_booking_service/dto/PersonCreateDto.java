package io.github.timur0o31.soa_booking_service.dto;

import jakarta.validation.Valid;
import java.time.LocalDate;

public record PersonCreateDto(@Valid LocalDate birthday, @Valid Integer weight,
                              @Valid LocationDto location) {
}
