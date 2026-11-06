package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BookingService {
    @Inject
    private TicketClient ticketClient;
    //я убрал @Transaction(Transactional.TxType.NOT_SUPPORTED), тут можно вообще не вызывать транзакцию
    public String createTicket(Long ticketId, Long personId, Float discount, String key) {
        return ticketClient.sell(ticketId, personId, discount, key);
    }

    public void cancelEvent(Long eventId) {
        ticketClient.cancelEvent(eventId);
    }
}
