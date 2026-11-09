package se.ifmo.ru.error.mappers;

import jakarta.annotation.Priority;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
@Priority(Priorities.USER - 100)
public class TransactionFailureMapper implements ExceptionMapper<TransactionalException> {
    @Override
    public Response toResponse(TransactionalException exception) {
        log.error("Ошибка при обработке запроса", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Не удалось завершить транзакцию");
    }
}
