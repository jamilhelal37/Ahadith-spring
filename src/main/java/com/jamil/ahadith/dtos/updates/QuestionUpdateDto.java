package com.jamil.ahadith.dtos.updates;

import lombok.Data;

import java.util.UUID;

@Data
public class QuestionUpdateDto {
    private UUID hadithId;
    private String askerText;
    private Boolean isActive;
    private String answerText;
}
