package com.jamil.ahadith.features.interaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerRequestDto {
    @NotBlank(message = "Answer text is required")
    private String answerText;
}
