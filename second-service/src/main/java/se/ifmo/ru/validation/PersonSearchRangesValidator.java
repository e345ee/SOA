package se.ifmo.ru.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;

public class PersonSearchRangesValidator implements RangesValidator,
        ConstraintValidator<ValidPersonSearchRanges, PersonSearchFilterDto> {
    @Override
    public boolean isValid(PersonSearchFilterDto filter, ConstraintValidatorContext context) {
        if (filter == null) return true;
        context.disableDefaultConstraintViolation();
        boolean validWeightRange = validateRange(filter.weightFrom(), filter.weightTo(),
                "weightTo", "weightFrom должен быть не больше personWeightTo", context);
        boolean weightValid = validateValueOrRange(filter.weight(),filter.weightFrom(),filter.weightTo(),
                "weight", context);
        boolean birthdayRangeValid = validateRange(filter.birthdayFrom(), filter.birthdayTo(), "birthdayTo",
                "birthdayFrom должен быть не позже birthdayTo", context);
        return weightValid && validWeightRange && birthdayRangeValid;
    }
}
