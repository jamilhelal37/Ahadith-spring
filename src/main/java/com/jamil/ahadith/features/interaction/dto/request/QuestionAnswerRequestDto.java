package com.jamil.ahadith.features.interaction.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerRequestDto {
    @NotBlank(message = "Answer text is required")
    @Size(max = ValidationLimits.ANSWER_MAX)
    private String answerText;
}
