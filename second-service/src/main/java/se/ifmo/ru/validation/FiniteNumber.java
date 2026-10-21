package se.ifmo.ru.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FiniteNumber.Validator.class)
public @interface FiniteNumber {
    String message() default "Expected a finite number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<FiniteNumber, Number> {
        @Override
        public boolean isValid(Number value, ConstraintValidatorContext context) {
            return value == null || Double.isFinite(value.doubleValue());
        }
    }
}
