package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.request.PersonSearchFilter;
import java.util.Map;
import java.util.Set;

public final class PersonSearchValidator {
    private static final Set<String> SORT_FIELDS = Set.of("id", "birthday", "weight", "location.x", "location.y", "location.name");

    public static Map<String, Object> validateSearch(SearchDto<PersonSearchFilter> request) {
        SearchValidation.validateSorts(request.getSort(), SORT_FIELDS);
        return request.getFilter() == null ? Map.of() : request.getFilter().toCriteria();
    }
}
