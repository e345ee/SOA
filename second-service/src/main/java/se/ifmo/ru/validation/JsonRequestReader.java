package se.ifmo.ru.validation;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import se.ifmo.ru.error.exceptions.RequestValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Priority(Priorities.ENTITY_CODER - 100)
public class JsonRequestReader implements MessageBodyReader<Object> {
    private static final ObjectMapper MAPPER = createMapper();

    private static ObjectMapper createMapper() {
        JavaTimeModule dates = new JavaTimeModule();
        dates.addDeserializer(LocalDate.class, new TextDateDeserializer<>(LocalDate.class, LocalDate::parse));
        dates.addDeserializer(OffsetDateTime.class, new TextDateDeserializer<>(OffsetDateTime.class, OffsetDateTime::parse));
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(dates)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.FAIL).withContentNulls(Nulls.FAIL));
        mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        mapper.coercionConfigFor(LogicalType.Float)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.TryConvert);
        return mapper;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getPackageName().equals("se.ifmo.ru.dto.request");
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> headers,
                           InputStream input) throws IOException {
        try {
            return MAPPER.readValue(input, MAPPER.constructType(genericType));
        } catch (JsonProcessingException exception) {
            throw new RequestValidationException("$", "Некорректный JSON, тип или формат значения");
        }
    }
}
