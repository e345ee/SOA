package probe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
@ApplicationScoped public class Context {
 @Inject Outer outer;
 @Inject Inner inner;
 @ActivateRequestContext public void run(){
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
