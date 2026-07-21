package com.jamil.ahadith.features.hadith.dto.request.reference;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class HadithReferenceRequestDto {
    @NotNull(message = "Hadith id is required")
    private UUID id;
}
