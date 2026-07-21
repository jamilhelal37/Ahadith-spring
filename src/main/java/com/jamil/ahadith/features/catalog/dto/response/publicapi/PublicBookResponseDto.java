package com.jamil.ahadith.features.catalog.dto.response.publicapi;

import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicBookResponseDto {
    private UUID id;
    private String name;
    private MuhaddithReferenceResponseDto muhaddith;
}
