package io.github.timur0o31.soa_booking_service.service;

import io.github.timur0o31.soa_booking_service.Client.TicketClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class BookingService {
    @Inject
    private TicketClient ticketClient;
    //я убрал @Transaction(Transactional.TxType.NOT_SUPPORTED), тут можно вообще не вызывать транзакцию
    public String createTicket(Long ticketId, Long personId, Float discount) {
        log.info("Запрос продажи билета: ticketId={}, personId={}, скидка={}", ticketId, personId, discount);
        return ticketClient.sell(ticketId, personId, discount);
    }

    public void cancelEvent(Long eventId) {
        log.info("Запрос отмены события: eventId={}", eventId);
        ticketClient.cancelEvent(eventId);
    }
}
