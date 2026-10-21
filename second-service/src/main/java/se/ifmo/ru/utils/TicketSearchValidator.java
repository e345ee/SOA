package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.TicketSearchFilter;
import java.util.Map;
import java.util.Set;

public final class TicketSearchValidator {
    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "coordinates.x", "coordinates.y", "creationDate", "price", "type",
            "person.birthday", "person.weight", "person.location.x", "person.location.y", "person.location.name", "eventId");

    public static Map<String, Object> validateSearch(SearchDto<TicketSearchFilter> request) {
        SearchValidation.validateSorts(request.getSort(), SORT_FIELDS);
        return request.getFilter() == null ? Map.of() : request.getFilter().toCriteria();
    }
}
