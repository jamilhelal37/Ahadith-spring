package com.jamil.ahadith.dtos.responses;

import com.jamil.ahadith.entities.SearchSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record SearchHistoryResponseDto(
        UUID id,
        String searchText,
        SearchSource searchSource,
        LocalDateTime createdAt
) {
}
