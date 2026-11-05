package se.ifmo.ru.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto<T>{
    @Valid
    private T filter;
    @NotNull @Min(0)
    private Long page = 0L;
    @NotNull @Min(1) @Max(100)
    private Long size = 20L;
    @Valid
    private List<@NotNull SortDto> sort;
}
