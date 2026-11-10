package probe;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import java.util.Objects;

@Interceptor
@Transactional(Transactional.TxType.SUPPORTS)
@Priority(Interceptor.Priority.APPLICATION + 100)
public class SupportsObserver {
    @Resource TransactionSynchronizationRegistry registry;
    @Inject PropagationState state;

    @AroundInvoke
    public Object verify(InvocationContext context) throws Exception {
        Transactional annotation = context.getMethod().getAnnotation(Transactional.class);
        if (state.observing() && annotation != null
                && annotation.value() == Transactional.TxType.SUPPORTS) {
            if (!Objects.equals(state.expectedTransaction(), registry.getTransactionKey())) {
                throw new IllegalStateException("SUPPORTS changed the transaction context");
            }
            state.recordRead();
        }
        return context.proceed();
    }
}
