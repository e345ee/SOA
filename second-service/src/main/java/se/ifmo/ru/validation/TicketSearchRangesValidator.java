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
        boolean validWeightRange = validateRange(filter.getPersonWeightFrom(), filter.getPersonWeightTo(),
                "personWeightTo", "personWeightFrom должен быть не больше personWeightTo", context);

        boolean xValid = validateRange(filter.getCoordinatesXFrom(), filter.getCoordinatesXTo(), "coordinatesXTo",
                "coordinatesXFrom должен быть не больше coordinatesXTo", context);
        boolean yValid = validateRange(filter.getCoordinatesYFrom(), filter.getCoordinatesYTo(),
                "coordinatesYTo", "coordinatesYFrom должен быть не больше coordinatesYTo", context);

        boolean birthdayRangeValid = validateRange(filter.getPersonBirthdayFrom(), filter.getPersonBirthdayTo(), "personBirthdayTo",
                "personBirthdayFrom должен быть не позже personBirthdayTo", context);
        var creationFrom = filter.getCreationDateFrom() == null ? null : filter.getCreationDateFrom().toInstant();
        var creationTo = filter.getCreationDateTo() == null ? null : filter.getCreationDateTo().toInstant();
        boolean creationDateValid = validateRange(creationFrom, creationTo, "creationDateTo",
                "creationDateFrom должен быть не позже creationDateTo", context);

        boolean weightChoiceValid = validateValueOrRange(filter.getPersonWeight(), filter.getPersonWeightFrom(), filter.getPersonWeightTo(),
                "personWeight", context);
        boolean priceRangeValid = validateRange(filter.getPriceFrom(), filter.getPriceTo(),
                "priceTo", "priceFrom должен быть не больше priceTo", context);
        return xValid && yValid && validWeightRange && birthdayRangeValid && creationDateValid && priceRangeValid && weightChoiceValid;
    }

}
