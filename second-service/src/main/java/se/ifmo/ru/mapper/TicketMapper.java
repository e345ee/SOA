package se.ifmo.ru.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import se.ifmo.ru.dto.request.TicketCreateDto;
import se.ifmo.ru.dto.request.TicketUpdateDto;
import se.ifmo.ru.dto.response.TicketResponseDto;
import se.ifmo.ru.model.Ticket;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = PersonMapper.class)
public interface TicketMapper {
    @Mapping(target="id", ignore=true)
    @Mapping(target="creationDate", ignore=true)
    @Mapping(target = "person", ignore = true)
    Ticket toTicket(TicketCreateDto ticketCreateDto);

    @Mapping(target="id", ignore=true)
    @Mapping(target="creationDate", ignore=true)
    @Mapping(target="person", ignore=true)
    void updateTicket(TicketUpdateDto ticketCreateDto, @MappingTarget Ticket ticket);

    TicketResponseDto toTicketResponseDto(Ticket ticket);
}
