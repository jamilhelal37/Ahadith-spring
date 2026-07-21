package com.jamil.ahadith.features.interaction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatusUpdateRequestDto {
    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
