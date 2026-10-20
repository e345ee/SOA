package io.github.timur0o31.soa_booking_service.mapper;

import io.github.timur0o31.soa_booking_service.dto.PersonCreateDto;
import io.github.timur0o31.soa_booking_service.dto.PersonResponseDto;
import io.github.timur0o31.soa_booking_service.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "jakarta-cdi")
public interface PersonMapper {
    Person toPerson(PersonCreateDto personCreateDto);
    PersonResponseDto toPersonResponseDto(Person person);
}
