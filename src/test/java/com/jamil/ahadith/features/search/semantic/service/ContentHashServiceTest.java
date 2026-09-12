package com.jamil.ahadith.features.search.semantic.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHashServiceTest {
    private final ContentHashService service = new ContentHashService();

    @Test
    void hashesExactUtf8TextWithSha256() {
        assertThat(service.sha256("hello"))
                .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        assertThat(service.sha256(" hello ")).isNotEqualTo(service.sha256("hello"));
    }
}
