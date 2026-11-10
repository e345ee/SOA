package probe;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class PropagationState {
    private boolean observing;
    private Object expectedTransaction;
    private int observedReads;
    private Long createdPersonId;

    public void begin(Object transaction) {
        expectedTransaction = transaction;
        observedReads = 0;
        observing = true;
    }
    public void end() { observing = false; }
    public boolean observing() { return observing; }
    public Object expectedTransaction() { return expectedTransaction; }
    public void recordRead() { observedReads++; }
    public int observedReads() { return observedReads; }
    public void createdPersonId(Long id) { createdPersonId = id; }
    public Long createdPersonId() { return createdPersonId; }
}
