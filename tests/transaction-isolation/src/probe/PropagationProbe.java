package probe;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.LocationDto;
import se.ifmo.ru.service.PersonService;
import se.ifmo.ru.service.TicketStatisticsService;
import java.time.LocalDate;

@ApplicationScoped
public class PropagationProbe {
    @Inject PropagationState state;
    @Inject TicketStatisticsService statistics;
    @Inject PersonService persons;
    @Resource TransactionSynchronizationRegistry registry;

    public static PersonCreateDto fixture() {
        return new PersonCreateDto(LocalDate.of(2000, 1, 1), 70,
                new LocationDto(0, 0, "transaction-probe-rollback"));
    }

    public void readsWithoutTransaction() {
        if (registry.getTransactionKey() != null) throw new IllegalStateException("Unexpected outer transaction");
        checkReads();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void readsInTransaction() {
        if (registry.getTransactionKey() == null) throw new IllegalStateException("Missing outer transaction");
        checkReads();
    }

    private void checkReads() {
        state.begin(registry.getTransactionKey());
        try {
            statistics.getSum();
            statistics.getAvg();
            statistics.countByPerson(-1L);
            if (state.observedReads() != 3) throw new IllegalStateException("SUPPORTS observer did not run");
        } finally {
            state.end();
        }
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void createAndRollback() {
        state.createdPersonId(persons.savePersonEntity(fixture()).getId());
        throw new RollbackProbeException();
    }

    public static class RollbackProbeException extends RuntimeException {}
}
