package com.jamil.ahadith.features.search.semantic.repository;

import java.util.UUID;

public record HadithEmbeddingState(UUID hadithId, String text) {
}
