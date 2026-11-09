package se.ifmo.ru.error.mappers;
import jakarta.json.stream.JsonParsingException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class JsonExceptionMapper implements ExceptionMapper<ProcessingException> {
    @Override
    public Response toResponse(ProcessingException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof JsonParsingException) {
                return ErrorResponses.build(Response.Status.BAD_REQUEST,
                        "Некорректный формат JSON");
            }
            cause = cause.getCause();
        }
        log.error("Ошибка при обработке запроса", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера");
    }
}