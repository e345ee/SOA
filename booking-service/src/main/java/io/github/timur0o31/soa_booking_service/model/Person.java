package io.github.timur0o31.soa_booking_service.model;

import lombok.Data;

import java.util.Date;

@Data
public class Person {
    private Long id;

    private Date birthday;

    private Integer weight;

    private Location location;

}
