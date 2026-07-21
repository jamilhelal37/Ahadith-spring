package com.jamil.ahadith.features.hadith.dto.request.reference;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FakeHadithReferenceRequestDto {
    @NotNull(message = "Fake hadith id is required")
    private UUID id;
}
