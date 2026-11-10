package se.ifmo.ru.isolation;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
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
    @Resource
    TransactionSynchronizationRegistry transactionRegistry;

    @AroundInvoke
    public Object setIsolation(InvocationContext context) throws Exception {
        if (transactionRegistry.getTransactionStatus() != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("@TransactionIsolation требует активную транзакцию");
        }
        TransactionIsolation annotation = context.getMethod().getAnnotation(TransactionIsolation.class);
        if (annotation == null) {
            annotation = context.getTarget().getClass().getAnnotation(TransactionIsolation.class);
        }
        if (annotation == null) {
            throw new IllegalStateException("Для перехваченного метода не задан уровень изоляции");
        }
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
