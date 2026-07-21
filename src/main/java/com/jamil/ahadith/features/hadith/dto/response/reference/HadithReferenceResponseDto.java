package com.jamil.ahadith.features.hadith.dto.response.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HadithReferenceResponseDto {
    private UUID id;
    private Integer hadithNumber;
    private String text;
}
