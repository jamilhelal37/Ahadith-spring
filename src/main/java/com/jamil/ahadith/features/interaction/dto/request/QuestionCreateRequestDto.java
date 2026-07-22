package com.jamil.ahadith.features.interaction.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = ValidationLimits.QUESTION_MAX)
    private String askerText;
}
