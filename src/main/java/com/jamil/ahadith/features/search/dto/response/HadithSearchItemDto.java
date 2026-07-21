package com.jamil.ahadith.features.search.dto.response;

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
public class HadithSearchItemDto {
    private UUID id;
    private String text;
    private String normalText;
    private Integer hadithNumber;
    private String type;
    private String sanad;
    private BookReferenceResponseDto book;
    private RawiReferenceResponseDto rawi;
    private RulingReferenceResponseDto ruling;
    private MuhaddithReferenceResponseDto muhaddith;
    private List<TopicReferenceResponseDto> topics;
    private boolean hasExplanation;
    private boolean hasSubValid;
}
