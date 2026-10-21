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
public class SearchDto<F> {
    @Valid
    private F filter;
    @NotNull @Min(0) @Max(1_000_000)
    private Long page = 0L;
    @NotNull @Min(1) @Max(100)
    private Long size = 20L;
    private List<SortDto> sort;
}
