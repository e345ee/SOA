package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.mapper.TicketMapper;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

@ApplicationScoped
public class BookingService {
    @Inject
    private TicketClient ticketClient;
    @Inject
    TicketMapper ticketMapper;
    public TicketResponseDto createTicket(Long ticketId, Long personId, Float discount){
        if (ticketId == null || ticketId <= 0) {
            throw new RequestValidationException("ticketId", "Идентификатор должен быть положительным");
        }
        if (personId == null || personId <= 0) {
            throw new RequestValidationException("personId", "Идентификатор должен быть положительным");
        }
        if (discount == null || !Float.isFinite(discount) || discount < 0 || discount > 100) {
            throw new RequestValidationException("discount", "Скидка должна быть от 0 включительно до 100 включительно");
        }
        Ticket ticket = ticketClient.getTicketById(ticketId);
        TicketCreateDto ticketCreateDto = ticketMapper.toTicketCreateDto(ticket);
        ticketCreateDto.setPersonId(personId);
        float price=ticket.getPrice()-ticket.getPrice()*discount/100f;
        if(!Float.isFinite(price)||price<=0)throw new RequestValidationException("discount","После скидки цена должна оставаться больше нуля");
        ticketCreateDto.setPrice(price);
        Ticket ticketResponse = ticketClient.createTicket(ticketCreateDto);
        return ticketMapper.toTicketResponseDto(ticketResponse);
    }
    public void cancelEvent(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new RequestValidationException("eventId", "Идентификатор должен быть положительным");
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
