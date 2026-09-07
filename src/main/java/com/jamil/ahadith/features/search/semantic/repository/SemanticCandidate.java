package com.jamil.ahadith.features.search.semantic.repository;

import java.util.UUID;

public record SemanticCandidate(UUID hadithId, double similarity) {
}
