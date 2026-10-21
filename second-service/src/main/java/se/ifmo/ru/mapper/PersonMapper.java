package se.ifmo.ru.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.model.Person;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface PersonMapper {
    @Mapping(target="id", ignore=true)
    Person toPerson(PersonCreateDto personCreateDto);
    PersonResponseDto toPersonResponseDto(Person person);

    @Mapping(target = "id", ignore = true)
    void updatePersonFromDto(PersonCreateDto dto, @MappingTarget Person person);
}
