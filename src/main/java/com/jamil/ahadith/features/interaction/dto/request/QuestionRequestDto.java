package com.jamil.ahadith.features.interaction.dto.request;

import com.jamil.ahadith.features.interaction.entity.Question;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class QuestionRequestDto {
    private UUID hadithId;
    @NotBlank(message = "Question text is required")
    private String askerText;
}
