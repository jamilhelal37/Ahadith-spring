package com.jamil.ahadith.features.catalog.dto.request;



import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RawiRequestDto {
    @NotBlank(message = "Rawi name is required")
    @Size(min = 2, message = "Rawi name must be at least 2 characters")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;
    @NotNull(message = "Rawi gender is required")
    private Gender gender;
    @NotBlank(message = "Rawi about is required")
    @Size(min = 10, message = "Rawi about must be at least 10 characters")
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String about;
}
