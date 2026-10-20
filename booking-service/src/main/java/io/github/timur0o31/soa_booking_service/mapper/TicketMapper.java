package io.github.timur0o31.soa_booking_service.mapper;

import io.github.timur0o31.soa_booking_service.dto.PersonResponseDto;
import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.model.Person;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "jakarta-cdi")
public interface TicketMapper {
    Ticket toTicket(TicketCreateDto ticketCreateDto);
    TicketResponseDto toTicketResponseDto(Ticket ticket);
    TicketCreateDto toTicketCreateDto(Ticket ticket);
    default PersonResponseDto toPersonResponseDto(Person person){
        return Mappers.getMapper(PersonMapper.class).toPersonResponseDto(person);
    }
}
