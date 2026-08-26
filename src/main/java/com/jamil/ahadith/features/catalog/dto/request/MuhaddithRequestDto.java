package com.jamil.ahadith.features.catalog.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MuhaddithRequestDto {
    @NotBlank(message = "Muhaddith name is required")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;

    @NotNull(message = "Muhaddith gender is required")
    private Gender gender;

    @NotBlank(message = "Muhaddith about is required")
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String about;
}
