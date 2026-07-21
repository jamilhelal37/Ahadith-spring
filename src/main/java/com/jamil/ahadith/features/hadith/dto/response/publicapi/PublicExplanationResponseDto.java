package com.jamil.ahadith.features.hadith.dto.response.publicapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicExplanationResponseDto {
    private UUID id;
    private String text;
    private String normalText;
}
