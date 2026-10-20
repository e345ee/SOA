package se.ifmo.ru.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.model.Ticket;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = PersonMapper.class)
public interface TicketMapper {
    @Mapping(target="coordinates", source="coordinatesDto")
    @Mapping(target="type", source="ticketType")
    @Mapping(target = "person", ignore = true)
    Ticket toTicket(TicketCreateDto ticketCreateDto);

    @Mapping(target="id", ignore=true)
    @Mapping(target="creationDate", ignore=true)
    @Mapping(target="person", ignore=true)
    @Mapping(target="coordinates", source="coordinatesDto")
    @Mapping(target="type", source="ticketType")
    void updateTicket(TicketCreateDto ticketCreateDto, @MappingTarget Ticket ticket);

    @Mapping(target="ticketType", source="type")
    TicketResponseDto toTicketResponseDto(Ticket ticket);
}
