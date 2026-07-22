package com.jamil.ahadith.features.catalog.dto.update;



import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RawiUpdateDto {
    @Size(min = 2, message = "Rawi name must be at least 2 characters")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;

    private Gender gender;

    @Size(min = 10, message = "Rawi about must be at least 10 characters")
    @Size(max = ValidationLimits.EXPLANATION_TEXT_MAX)
    private String about;
}
