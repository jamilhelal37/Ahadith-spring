package com.jamil.ahadith.features.search.semantic.repository;

public record EmbeddingStatus(long totalHadiths, long current, long missing, long stale) {
}
