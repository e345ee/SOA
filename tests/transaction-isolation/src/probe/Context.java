package probe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
@ApplicationScoped public class Context {
 @Inject Outer outer;
 @Inject Inner inner;
 @Inject PropagationProbe propagation;
 @Inject PropagationState state;
 @Inject se.ifmo.ru.service.PersonService persons;
 @Inject se.ifmo.ru.repository.PersonRepository personRepository;
 @jakarta.transaction.Transactional(jakarta.transaction.Transactional.TxType.NOT_SUPPORTED)
 @ActivateRequestContext public void run(){
  propagation.readsWithoutTransaction();
  propagation.readsInTransaction();
  boolean mandatoryRejected = false;
  try { persons.savePersonEntity(PropagationProbe.fixture()); }
  catch (Exception e) {
   for (Throwable t=e; t!=null; t=t.getCause())
    if (t instanceof jakarta.transaction.TransactionRequiredException) mandatoryRejected=true;
  }
  if (!mandatoryRejected) throw new IllegalStateException("MANDATORY did not reject missing transaction");
  boolean rolledBack = false;
  try { propagation.createAndRollback(); }
  catch (Exception e) {
   for (Throwable t=e; t!=null; t=t.getCause())
    if (t instanceof PropagationProbe.RollbackProbeException) rolledBack=true;
  }
  if (!rolledBack || state.createdPersonId() == null || personRepository.findById(state.createdPersonId()).isPresent())
   throw new IllegalStateException("MANDATORY did not participate in outer rollback");
  System.out.println("TX_PROBE SUPPORTS and MANDATORY PASS (fixture rolled back)");
  outer.run();
  if(!inner.override()[1].equals("read committed")) throw new IllegalStateException("Method annotation did not override class");
  boolean rejected=false;
  try {inner.invalid();} catch(Exception e){
   for(Throwable t=e;t!=null;t=t.getCause()) if(t.getMessage()!=null && t.getMessage().contains("требует @Transactional(REQUIRES_NEW)")) rejected=true;
  }
  if(!rejected) throw new IllegalStateException("REQUIRED was not rejected by isolation guard");
  System.out.println("TX_PROBE guard and annotation precedence PASS");
 }
}
