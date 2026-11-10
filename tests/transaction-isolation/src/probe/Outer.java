package probe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.ifmo.ru.repository.TicketRepository;
import se.ifmo.ru.service.TicketService;
@ApplicationScoped public class Outer {
 @Inject TicketRepository repo;
 @Inject TicketService service;
 @Inject Inner inner;
 @Transactional(Transactional.TxType.REQUIRED) public void run(){
  Object[] before=read();
  service.getTickets(0,1);
  Object[] nested=inner.read();
  Object[] after=read();
  System.out.println("TX_PROBE before="+java.util.Arrays.toString(before)+" nested="+java.util.Arrays.toString(nested)+" after="+java.util.Arrays.toString(after));
  if(!before[0].toString().equals(after[0].toString()) || before[0].toString().equals(nested[0].toString()) || !nested[1].equals("repeatable read") || !before[1].equals(after[1])) throw new IllegalStateException("Transaction/snapshot mismatch");
 }
 private Object[] read(){return (Object[])repo.session().createNativeQuery("select txid_current(), current_setting('transaction_isolation')",Object[].class).getSingleResult();}
}
