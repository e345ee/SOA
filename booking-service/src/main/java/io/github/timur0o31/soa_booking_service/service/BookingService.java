package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.mapper.TicketMapper;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class BookingService {
    @Inject
    private TicketClient ticketClient;
    @Inject
    TicketMapper ticketMapper;
    public TicketResponseDto createTicket(Long ticketId, Long personId, Float discount){
        Ticket ticket = ticketClient.getTicketById(ticketId);
        TicketCreateDto ticketCreateDto = ticketMapper.toTicketCreateDto(ticket);
        ticketCreateDto.setPersonId(personId);
        ticketCreateDto.setPrice(ticket.getPrice()-ticket.getPrice()*discount/100f);
        Ticket ticketResponse = ticketClient.createTicket(ticketCreateDto);
        return ticketMapper.toTicketResponseDto(ticketResponse);
    }
    public void cancelEvent(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new BadRequestException("Некорректный eventId");
        }
        List<Long> ticketIds = ticketClient.getAllTicketIdsByEventId(eventId);
        if (ticketIds.isEmpty()) {
            throw new NotFoundException("Билеты на событие не найдены");
        }
        for (Long ticketId : ticketIds) {
            ticketClient.deleteTicketById(ticketId);
        }
    }
}
