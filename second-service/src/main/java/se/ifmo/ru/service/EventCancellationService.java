package se.ifmo.ru.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;

@ApplicationScoped
public class EventCancellationService {
    @Resource(lookup = "java:comp/UserTransaction")
    private UserTransaction transaction;

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void cancelEvent(long eventId) {
        try {
            transaction.begin();
            int deleted = entityManager.createQuery("DELETE FROM Ticket t WHERE t.eventId = :eventId")
                    .setParameter("eventId", eventId)
                    .executeUpdate();
            if (deleted == 0) {
                throw new ResourceNotFoundException("Билеты на событие не найдены");
            }
            transaction.commit();
        } catch (Exception exception) {
            rollback(exception);
            if (exception instanceof RuntimeException failure) throw failure;
            throw new IllegalStateException("Не удалось отменить событие", exception);
        }
    }

    private void rollback(Exception failure) {
        try {
            int status = transaction.getStatus();
            if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                transaction.rollback();
            }
        } catch (Exception rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }
}
