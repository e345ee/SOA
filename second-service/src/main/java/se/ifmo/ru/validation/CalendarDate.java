package se.ifmo.ru.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import static java.lang.annotation.ElementType.*;

@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CalendarDate.Validator.class)
public @interface CalendarDate {
    String message() default "Expected a calendar date with year 1..9999";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<CalendarDate, TemporalAccessor> {
        @Override
        public boolean isValid(TemporalAccessor value, ConstraintValidatorContext context) {
            return value == null || (value.get(ChronoField.YEAR) >= 1 && value.get(ChronoField.YEAR) <= 9999);
        }
    }
}
