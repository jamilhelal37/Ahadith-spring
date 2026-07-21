package com.jamil.ahadith.features.catalog.dto.request.reference;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookReferenceRequestDto {
    @NotNull(message = "Book id is required")
    private UUID id;
}
