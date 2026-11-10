package se.ifmo.ru.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.repository.TicketRepository;
// по идее вообще можно было засунуть это внутрь ticketService
@Slf4j
@ApplicationScoped
public class EventCancellationService {
    @Inject
    private TicketRepository ticketRepository;
    @Transactional(Transactional.TxType.REQUIRED)
    public void cancelEvent(long eventId) {
        log.info("Начало удаления билетов по eventId={}", eventId);
        int deleted = ticketRepository.deleteByEventId(eventId);
        log.info("Запрос на удаление билетов выполнен: eventId={}, количество={}", eventId, deleted);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Билеты на событие не найдены");
        }
    }
}
