package se.ifmo.ru.isolation;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import java.lang.annotation.Annotation;
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
        Transactional transactional = annotation(context, Transactional.class);
        if (transactional == null || transactional.value() != Transactional.TxType.REQUIRES_NEW) {
            throw new IllegalStateException("@TransactionIsolation требует @Transactional(REQUIRES_NEW): "
                    + context.getMethod().getName());
        }
        if (transactionRegistry.getTransactionStatus() != Status.STATUS_ACTIVE) {
            throw new IllegalStateException("@TransactionIsolation требует активную транзакцию");
        }
        TransactionIsolation annotation = annotation(context, TransactionIsolation.class);
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

    private static <A extends Annotation> A annotation(InvocationContext context, Class<A> type) {
        A value = context.getMethod().getAnnotation(type);
        if (value != null) return value;
        for (Class<?> target = context.getTarget().getClass(); target != null; target = target.getSuperclass()) {
            value = target.getDeclaredAnnotation(type);
            if (value != null) return value;
        }
        return null;
    }
}
