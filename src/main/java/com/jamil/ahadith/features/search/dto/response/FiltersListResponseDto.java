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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltersListResponseDto {
    private List<RulingReferenceResponseDto> rulings;
    private List<RawiReferenceResponseDto> rawis;
    private List<MuhaddithReferenceResponseDto> muhaddiths;
    private List<BookReferenceResponseDto> books;
    private List<TopicReferenceResponseDto> topics;
}
