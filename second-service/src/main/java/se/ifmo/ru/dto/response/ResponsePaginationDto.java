package se.ifmo.ru.dto.response;

import java.util.List;

public record ResponsePaginationDto<T>(List<T> content, Long page, Long size, long totalElements, long totalPages, boolean hasNext, boolean hasPrevious) {
    public static <T> ResponsePaginationDto<T> ans(List<T> content, Long page, Long size, long totalElements) {
        long totalPages = totalElements / size + (totalElements % size == 0 ? 0 : 1);
        return new ResponsePaginationDto<>(content, page, size, totalElements,
                totalPages, page < totalPages - 1, page > 0);
    }
}
