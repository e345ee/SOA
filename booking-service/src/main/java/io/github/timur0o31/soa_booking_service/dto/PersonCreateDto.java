package io.github.timur0o31.soa_booking_service.dto;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.Valid;

import java.util.Date;

public record PersonCreateDto(@Valid Date birthday, @Valid Integer weight,
                              @Valid LocationDto location) {
}
