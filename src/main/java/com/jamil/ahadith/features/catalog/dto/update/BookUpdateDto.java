package com.jamil.ahadith.features.catalog.dto.update;

import com.jamil.ahadith.features.catalog.dto.request.reference.MuhaddithReferenceRequestDto;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class BookUpdateDto {
    private String name;
    @Valid
    private MuhaddithReferenceRequestDto muhaddith;
}
