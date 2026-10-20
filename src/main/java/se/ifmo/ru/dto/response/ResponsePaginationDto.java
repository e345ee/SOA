package se.ifmo.ru.dto.response;

import java.util.List;

public record ResponsePaginationDto<T>(List<T> content, Long page, Long size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
    public static <T> ResponsePaginationDto<T> ans(List<T> content, Long page, Long size, long totalElements) {
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new ResponsePaginationDto<>(content, page, size, totalElements,
                totalPages, page < totalPages, page > 1);
    }
}
