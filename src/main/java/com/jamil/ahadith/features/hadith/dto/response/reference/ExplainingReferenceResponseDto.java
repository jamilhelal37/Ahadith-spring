package com.jamil.ahadith.features.hadith.dto.response.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplainingReferenceResponseDto {
    private UUID id;
    private String text;
}
