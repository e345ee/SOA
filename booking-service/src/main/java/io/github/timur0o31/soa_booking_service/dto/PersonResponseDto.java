package io.github.timur0o31.soa_booking_service.dto;

import jakarta.validation.Valid;

import java.util.Date;

public record PersonResponseDto(Long id, @Valid Date birthDate, @Valid Integer weight,
                                @Valid LocationDto locationDto){
}
