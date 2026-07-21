package com.jamil.ahadith.features.hadith.dto.response.publicapi;

import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicHadithSummaryResponseDto {
    private UUID id;
    private String text;
    private String normalText;
    private Integer hadithNumber;
    private String type;
    private String sanad;
    private MuhaddithReferenceResponseDto muhaddith;
    private RawiReferenceResponseDto rawi;
    private BookReferenceResponseDto book;
    private RulingReferenceResponseDto ruling;
}
