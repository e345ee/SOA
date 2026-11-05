package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BookingService {
    @Inject
    private TicketClient ticketClient;

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public String createTicket(Long ticketId, Long personId, Float discount, String key) {
        return ticketClient.sell(ticketId, personId, discount, key);
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void cancelEvent(Long eventId) {
        ticketClient.cancelEvent(eventId);
    }
}
