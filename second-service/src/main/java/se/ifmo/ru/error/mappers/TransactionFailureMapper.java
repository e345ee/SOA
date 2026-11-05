package se.ifmo.ru.error.mappers;

import jakarta.annotation.Priority;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER - 100)
public class TransactionFailureMapper implements ExceptionMapper<TransactionalException> {

    @Override
    public Response toResponse(TransactionalException exception) {
        io.github.timur0o31.soa.log.RequestTrace.failure(exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Не удалось завершить транзакцию");
    }
}
