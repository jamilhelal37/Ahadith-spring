package com.jamil.ahadith.features.search.semantic.client;

import java.util.List;

public record EmbeddingResponse(
        String model,
        String modelVersion,
        int dimension,
        List<List<Double>> embeddings
) {
}
