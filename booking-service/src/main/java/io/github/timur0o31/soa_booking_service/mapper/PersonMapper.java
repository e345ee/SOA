package io.github.timur0o31.soa_booking_service.mapper;

import io.github.timur0o31.soa_booking_service.dto.PersonCreateDto;
import io.github.timur0o31.soa_booking_service.dto.PersonResponseDto;
import io.github.timur0o31.soa_booking_service.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "jakarta-cdi")
public interface PersonMapper {
    @Mapping(target="id", ignore=true)
    Person toPerson(PersonCreateDto personCreateDto);
    PersonResponseDto toPersonResponseDto(Person person);
}
