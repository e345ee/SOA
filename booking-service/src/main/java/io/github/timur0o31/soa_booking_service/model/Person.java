package io.github.timur0o31.soa_booking_service.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Person {
    private Long id;

    private LocalDate birthday;

    private Integer weight;

    private Location location;

}
