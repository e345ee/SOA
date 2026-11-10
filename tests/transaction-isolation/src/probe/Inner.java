package probe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.isolation.TransactionIsolation;
import se.ifmo.ru.enums.IsolationType;
@ApplicationScoped @Transactional(Transactional.TxType.REQUIRES_NEW) @TransactionIsolation(IsolationType.REPEATABLE_READ)
public class Inner {
 @Inject TicketRepository repo;
 public Object[] read(){return (Object[])repo.session().createNativeQuery("select txid_current(), current_setting('transaction_isolation')",Object[].class).getSingleResult();}
 @Transactional(Transactional.TxType.REQUIRED)
 public void invalid(){throw new IllegalStateException("BODY_REACHED");}
 @Transactional(Transactional.TxType.REQUIRES_NEW) @TransactionIsolation(IsolationType.READ_COMMITTED)
 public Object[] override(){return read();}
}

