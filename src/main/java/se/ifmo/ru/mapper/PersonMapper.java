package se.ifmo.ru.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.model.Person;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface PersonMapper {
    @Mapping(target="location.name", source="locationDto.name")
    @Mapping(target="location.x", source="locationDto.x")
    @Mapping(target="location.y", source="locationDto.y")
    Person toPerson(PersonCreateDto personCreateDto);
    @Mapping(target="locationDto", source="location")
    PersonResponseDto toPersonResponseDto(Person person);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "locationDto")
    void updatePersonFromDto(PersonCreateDto dto, @MappingTarget Person person);
}
