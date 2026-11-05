package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto<T>{
    @Valid
    private T filter;
    private Long page = 0L;
    private Long size = 20L;
    private List<SortDto> sort;
}
