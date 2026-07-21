package com.jamil.ahadith.features.catalog.dto.response.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReferenceResponseDto {
    private UUID id;
    private String name;
}
