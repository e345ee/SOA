package io.github.timur0o31.soa_booking_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Location {
    private Integer x;
    private Integer y;
    private String name;

}
