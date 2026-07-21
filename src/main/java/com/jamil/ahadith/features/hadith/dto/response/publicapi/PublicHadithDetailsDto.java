package com.jamil.ahadith.features.hadith.dto.response.publicapi;

import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicHadithDetailsDto {
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
    private List<TopicReferenceResponseDto> topics;
    private PublicExplanationResponseDto explanation;
    private PublicHadithSummaryResponseDto validAlternative;
    private long commentsCount;
    private HadithViewerStateDto viewerState;
}
