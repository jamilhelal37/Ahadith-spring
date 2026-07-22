package com.jamil.ahadith.features.catalog.dto.update;


import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MuhaddithUpdateDto {
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;
    private Gender gender;
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String about;
}
