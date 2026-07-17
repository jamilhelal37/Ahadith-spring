package com.jamil.ahadith.features.interaction.dto.update;

import lombok.Data;

import java.util.UUID;

@Data
public class QuestionUpdateDto {
    private UUID hadithId;
    private String askerText;
    private Boolean isActive;
    private String answerText;
}
