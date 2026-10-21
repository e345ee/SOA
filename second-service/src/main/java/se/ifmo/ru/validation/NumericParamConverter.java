package se.ifmo.ru.validation;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.Function;

@Provider
public class NumericParamConverter implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> type, Type genericType, Annotation[] annotations) {
        Function<String, ?> parser = type == Long.class ? Long::valueOf
                : type == Integer.class ? Integer::valueOf
                : type == Float.class ? NumericParamConverter::percentage : null;
        if (parser == null) return null;
        return new ParamConverter<>() {
            public T fromString(String value) {
                try {
                    return type.cast(parser.apply(value));
                } catch (NumberFormatException exception) {
                    throw new BadRequestException("Invalid numeric parameter", exception);
                }
            }

            public String toString(T value) {
                return value.toString();
            }
        };
    }

    private static float percentage(String value) {
        BigDecimal number = new BigDecimal(value);
        float result = number.floatValue();
        if (!Float.isFinite(result) || (result == 0 && number.signum() != 0)) {
            throw new NumberFormatException("Outside float range");
        }
        return result;
    }
}
