package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import io.github.timur0o31.soa_booking_service.dto.TicketCreateDto;
import io.github.timur0o31.soa_booking_service.dto.TicketResponseDto;
import io.github.timur0o31.soa_booking_service.mapper.TicketMapper;
import io.github.timur0o31.soa_booking_service.model.Ticket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.github.timur0o31.soa_booking_service.error.exceptions.RequestValidationException;

@ApplicationScoped
public class BookingService {
    private static final float MINIMUM_TICKET_PRICE = 1.0f;

    @Inject
    private TicketClient ticketClient;
    @Inject
    TicketMapper ticketMapper;
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
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
        double discountedPrice = (double) ticket.getPrice() * (1.0d - discount / 100.0d);
        if (!Double.isFinite(discountedPrice)) {
            throw new RequestValidationException("discount", "Не удалось рассчитать конечную цену билета");
        }
        float price = (float) Math.max(MINIMUM_TICKET_PRICE, discountedPrice);
        ticketCreateDto.setPrice(price);
        Ticket ticketResponse = ticketClient.createTicket(ticketCreateDto);
        return ticketMapper.toTicketResponseDto(ticketResponse);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void cancelEvent(Long eventId) {
        ticketClient.cancelEvent(eventId);
    }
}
