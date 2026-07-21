package com.jamil.ahadith.features.interaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequestDto {
    private UUID hadithId;

    @NotBlank(message = "Question text is required")
    private String askerText;
}
