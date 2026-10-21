package se.ifmo.ru.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.DateTimeException;
import java.util.function.Function;

final class TextDateDeserializer<T> extends StdDeserializer<T> {
    private final Function<String, T> parse;

    TextDateDeserializer(Class<T> type, Function<String, T> parse) {
        super(type);
        this.parse = parse;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw context.wrongTokenException(parser, handledType(), JsonToken.VALUE_STRING, "Expected an ISO date string");
        }
        try {
            return parse.apply(parser.getText());
        } catch (DateTimeException exception) {
            throw context.weirdStringException(parser.getText(), handledType(), "Invalid ISO calendar date");
        }
    }
}
