package com.jamil.ahadith.features.search.dto.response;

import com.jamil.ahadith.features.search.entity.SearchSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record SearchHistoryResponseDto(
        UUID id,
        String searchText,
        SearchSource searchSource,
        LocalDateTime createdAt
) {
}
