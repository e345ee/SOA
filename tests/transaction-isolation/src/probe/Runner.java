package probe;
import jakarta.ejb.*;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
@Singleton @Startup @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class Runner {
 @Inject Context context;
 @PostConstruct public void start(){
  try { context.run(); System.out.println("TX_PROBE PASS"); }
  catch(Exception e){System.out.println("TX_PROBE FAIL " + e); e.printStackTrace();}
 }
}
