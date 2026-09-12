package com.jamil.ahadith.features.search.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchModeTest {
    @Test
    void semanticAndHybridModesAreSupported() {
        assertThat(SearchMode.valueOf("SEMANTIC")).isEqualTo(SearchMode.SEMANTIC);
        assertThat(SearchMode.valueOf("HYBRID")).isEqualTo(SearchMode.HYBRID);
    }
}
