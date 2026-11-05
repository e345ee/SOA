package se.ifmo.ru.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
public class TicketSearchRangesValidator implements RangesValidator,
        ConstraintValidator<ValidTicketsSearchRanges, TicketSearchFilterDto> {

    @Override
    public boolean isValid(TicketSearchFilterDto filter, ConstraintValidatorContext context) {
        if (filter == null) return true;
        context.disableDefaultConstraintViolation();
        boolean validWeightRange = validateRange(filter.personWeightFrom(), filter.personWeightTo(),
                "personWeightTo", "personWeightFrom должен быть не больше personWeightTo", context);
        boolean xValid = validateRange(filter.coordinatesXFrom(), filter.coordinatesXTo(), "coordinatesXTo",
                "coordinatesXFrom должен быть не больше coordinatesXTo", context);
        boolean yValid = validateRange(filter.coordinatesYFrom(), filter.coordinatesYTo(),
                "coordinatesYTo", "coordinatesYFrom должен быть не больше coordinatesYTo", context);
        boolean weightValid = validateValueOrRange(filter.personWeight(),filter.personWeightFrom(),filter.personWeightTo(),
                "weight", context);
        boolean birthdayRangeValid = validateRange(filter.personBirthdayFrom(), filter.personBirthdayTo(), "personBirthdayTo",
                "personBirthdayFrom должен быть не позже personBirthdayTo", context);
        boolean creationDateValid = validateRange(filter.creationDateFrom(), filter.creationDateTo(), "creationDateTo",
                "creationDateFrom должен быть не позже creationDateTo", context);
        return xValid && yValid && weightValid &&
                validWeightRange && birthdayRangeValid && creationDateValid;
    }

}
