package se.ifmo.ru.dto.request;

import java.util.Map;

public interface SearchFilter {
    Map<String, Object> toCriteria();
}
