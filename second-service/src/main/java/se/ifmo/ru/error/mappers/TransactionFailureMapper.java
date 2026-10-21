package se.ifmo.ru.error.mappers;

import jakarta.annotation.Priority;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.USER - 100)
public class TransactionFailureMapper implements ExceptionMapper<TransactionalException> {
    private static final Logger LOG = Logger.getLogger(TransactionFailureMapper.class.getName());

    @Override
    public Response toResponse(TransactionalException exception) {
        LOG.log(Level.SEVERE, "Transaction failed", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Не удалось завершить транзакцию");
    }
}
