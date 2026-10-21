package io.github.timur0o31.soa_booking_service.dto;

import java.time.LocalDate;

public record PersonResponseDto(Long id, LocalDate birthday, Integer weight,
                                LocationDto locationDto){
}
