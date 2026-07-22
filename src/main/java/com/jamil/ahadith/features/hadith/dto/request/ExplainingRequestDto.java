package com.jamil.ahadith.features.hadith.dto.request;



import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExplainingRequestDto {
    @NotBlank(message = "Explaining text is required")
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String text;
}
