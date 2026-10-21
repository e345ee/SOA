package se.ifmo.ru.utils;
import se.ifmo.ru.dto.request.SearchDto;
import java.util.Map;
public final class TicketSearchValidator {
 public static Map<String,Object> validateSearch(SearchDto dto) { return ContractSearch.validate(dto,true); }
 public static String field(String key) { return key; }
}
