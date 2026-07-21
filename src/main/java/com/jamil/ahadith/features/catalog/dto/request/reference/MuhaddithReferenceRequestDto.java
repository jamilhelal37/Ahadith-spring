package com.jamil.ahadith.features.catalog.dto.request.reference;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MuhaddithReferenceRequestDto {
    @NotNull(message = "Muhaddith id is required")
    private UUID id;
}
