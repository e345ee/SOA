package se.ifmo.ru.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {
    private Map<String, Object> filter;
    private Long page;
    private Long size;
    private List<SortDto> sort;
}
