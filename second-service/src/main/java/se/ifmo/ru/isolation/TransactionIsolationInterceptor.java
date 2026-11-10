package se.ifmo.ru.isolation;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;
import org.hibernate.StatelessSession;
import se.ifmo.ru.enums.IsolationType;
import se.ifmo.ru.repository.TicketRepository;

import java.sql.Statement;

@Interceptor
@TransactionIsolation(IsolationType.READ_COMMITTED)
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionIsolationInterceptor {

    @Inject
    TicketRepository ticketRepository;

    @AroundInvoke
    public Object setIsolation(InvocationContext context) throws Exception {
        Transactional transactional = context.getMethod().getAnnotation(Transactional.class);
        if (transactional == null || transactional.value() != Transactional.TxType.REQUIRES_NEW) {
            throw new IllegalStateException("@TransactionIsolation требует @Transactional(REQUIRES_NEW)");
        }
        TransactionIsolation annotation = context.getMethod().getAnnotation(TransactionIsolation.class);
        IsolationType isolationType = annotation.value();
        StatelessSession session = ticketRepository.session();
        session.joinTransaction();
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET TRANSACTION ISOLATION LEVEL " + isolationType.getType());
            }
        });
        return context.proceed();
    }
}
