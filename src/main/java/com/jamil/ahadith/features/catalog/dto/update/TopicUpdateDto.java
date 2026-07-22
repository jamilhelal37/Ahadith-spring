package com.jamil.ahadith.features.catalog.dto.update;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicUpdateDto {
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;
}
