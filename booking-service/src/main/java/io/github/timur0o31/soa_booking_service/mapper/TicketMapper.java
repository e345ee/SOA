package io.github.timur0o31.soa_booking_service.mapper;

import io.github.timur0o31.soa_booking_service.dto.PersonResponseDto;
import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.model.Person;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.ERROR, componentModel = "jakarta-cdi")
public interface TicketMapper {
    @org.mapstruct.Mapping(target="id", ignore=true)
    @org.mapstruct.Mapping(target="creationDate", ignore=true)
    @org.mapstruct.Mapping(target="person", ignore=true)
    Ticket toTicket(TicketCreateDto ticketCreateDto);
    TicketResponseDto toTicketResponseDto(Ticket ticket);
    @org.mapstruct.Mapping(target="personId", ignore=true)
    TicketCreateDto toTicketCreateDto(Ticket ticket);
    default PersonResponseDto toPersonResponseDto(Person person){
        return Mappers.getMapper(PersonMapper.class).toPersonResponseDto(person);
    }
}
