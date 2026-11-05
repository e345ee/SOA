package se.ifmo.ru.validation;

import jakarta.validation.ConstraintValidatorContext;
// скорее всего лучше это сделать как util класс
public interface RangesValidator {
    default <T extends Comparable<? super T>> boolean isValidRange(T from, T to) {
        if (from == null || to == null) {
            return true;
        }
        return from.compareTo(to) <= 0;
    }

    default <T extends Comparable<? super T>> boolean validateRange(T from, T to, String field, String message,
                                                                    ConstraintValidatorContext context) {
        if (isValidRange(from, to)) return true;
        addMessage(context, field, message);
        return false;
    }

    default void addMessage(ConstraintValidatorContext context,String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
    default boolean validateValueOrRange(Object exact, Object from, Object to,
            String field, ConstraintValidatorContext context) {
        boolean exactSet = exact != null;
        boolean rangeSet = from != null || to != null;
        if (exactSet && rangeSet) {
            context.buildConstraintViolationWithTemplate("Точное значение нельзя передавать вместе с диапазоном")
                    .addPropertyNode(field)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
