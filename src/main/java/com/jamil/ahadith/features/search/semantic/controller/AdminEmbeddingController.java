package com.jamil.ahadith.features.search.semantic.controller;

import com.jamil.ahadith.features.search.semantic.repository.EmbeddingStatus;
import com.jamil.ahadith.features.search.semantic.service.HadithEmbeddingService;
import com.jamil.ahadith.features.search.semantic.service.ReindexResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/hadith-embeddings")
public class AdminEmbeddingController {
    private final HadithEmbeddingService service;

    @GetMapping("/status")
    public EmbeddingStatus status() {
        return service.status();
    }

    @PostMapping("/reindex")
    public ReindexResult reindex(@RequestParam(defaultValue = "false") boolean force) {
        return service.reindex(force);
    }
}
