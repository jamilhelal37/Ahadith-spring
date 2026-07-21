package com.jamil.ahadith.features.catalog.dto.request;

import com.jamil.ahadith.features.catalog.dto.request.reference.MuhaddithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookRequestDto {
    @NotBlank(message = "Book name is required")
    @Size(min = 2, message = "Book name must be at least 2 characters")
    private String name;
    @Valid
    @NotNull(message = "Muhaddith is required")
    private MuhaddithReferenceRequestDto muhaddith;
}
