package com.jamil.ahadith.features.catalog.dto.update;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.catalog.dto.request.reference.MuhaddithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookUpdateDto {
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;
    @Valid
    private MuhaddithReferenceRequestDto muhaddith;
}
