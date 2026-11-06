package se.ifmo.ru.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.repository.TicketRepository;

@ApplicationScoped
public class EventCancellationService {
    @Inject
    private TicketRepository ticketRepository;
    @Transactional
    public void cancelEvent(long eventId) {
        int deleted = ticketRepository.deleteByEventId(eventId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Билеты на событие не найдены");
        }
    }
}
