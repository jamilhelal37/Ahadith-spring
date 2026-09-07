package com.jamil.ahadith.features.search.semantic.event;

import java.util.UUID;

public record HadithTextChangedEvent(UUID hadithId, String text) {
}
