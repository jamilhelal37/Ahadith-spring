package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Hadith;
import lombok.Data;

@Data
public class QuestionUpdateDto {
    private Hadith hadith;
    private String askerText;
    private Boolean isActive;
    private String answerText;
}
