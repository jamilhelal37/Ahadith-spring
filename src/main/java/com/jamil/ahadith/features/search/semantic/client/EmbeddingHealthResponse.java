package com.jamil.ahadith.features.search.semantic.client;

public record EmbeddingHealthResponse(
        String status,
        String model,
        String modelVersion,
        int dimension,
        String device
) {
}
