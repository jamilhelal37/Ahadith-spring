package com.jamil.ahadith.features.catalog.dto.request;


import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RulingRequestDto {
    @NotBlank(message = "Ruling name is required")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;
}


