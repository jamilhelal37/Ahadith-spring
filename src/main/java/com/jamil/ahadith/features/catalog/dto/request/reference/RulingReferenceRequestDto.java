package com.jamil.ahadith.features.catalog.dto.request.reference;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RulingReferenceRequestDto {
    @NotNull(message = "Ruling id is required")
    private UUID id;
}
