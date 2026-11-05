package se.ifmo.ru.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import se.ifmo.ru.validation.ValidPersonSearchRanges;

import java.time.LocalDate;

@Data
@ValidPersonSearchRanges
public class PersonSearchFilterDto {
    @Positive private Long id;
    private LocalDate birthday;
    private LocalDate birthdayFrom;
    private LocalDate birthdayTo;
    @Positive private Integer weight;
    @Positive private Integer weightFrom;
    @Positive private Integer weightTo;
    private Integer locationX;
    private Integer locationY;
    private String locationNameContains;
}
