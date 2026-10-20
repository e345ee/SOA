package io.github.timur0o31.soa_booking_service.dto;

import jakarta.validation.Valid;

import java.util.Date;

public record PersonCreateDto(@Valid Date birthDate, @Valid Double weight,
                              @Valid LocationDto locationDto) {
}
