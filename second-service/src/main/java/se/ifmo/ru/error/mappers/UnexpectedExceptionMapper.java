package se.ifmo.ru.error.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Ошибка при обработке запроса", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }
}
