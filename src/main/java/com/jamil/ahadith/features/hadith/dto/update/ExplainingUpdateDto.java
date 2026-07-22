package com.jamil.ahadith.features.hadith.dto.update;


import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExplainingUpdateDto {
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String text;
}
