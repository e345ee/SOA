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
        boolean validWeightRange = validateRange(filter.getWeightFrom(), filter.getWeightTo(),
                "weightTo", "weightFrom должен быть не больше weightTo", context);
        boolean birthdayRangeValid = validateRange(filter.getBirthdayFrom(), filter.getBirthdayTo(), "birthdayTo",
                "birthdayFrom должен быть не позже birthdayTo", context);
        boolean weightChoiceValid = validateValueOrRange(filter.getWeight(), filter.getWeightFrom(), filter.getWeightTo(),
                "weight", context);
        return validWeightRange && birthdayRangeValid && weightChoiceValid;
    }
}
