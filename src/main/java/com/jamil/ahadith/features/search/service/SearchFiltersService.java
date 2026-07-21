package com.jamil.ahadith.features.search.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jamil.ahadith.core.config.PublicCacheProperties;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.search.dto.response.FiltersListResponseDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchFiltersService {
    private static final String FILTERS_LIST_CACHE_KEY = "filters-list";

    private final RulingRepository rulingRepository;
    private final RawiRepository rawiRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final BookRepository bookRepository;
    private final TopicRepository topicRepository;
    private final MeterRegistry meterRegistry;
    private final Cache<String, FiltersListResponseDto> filtersListCache;

    public SearchFiltersService(
            RulingRepository rulingRepository,
            RawiRepository rawiRepository,
            MuhaddithRepository muhaddithRepository,
            BookRepository bookRepository,
            TopicRepository topicRepository,
            MeterRegistry meterRegistry,
            PublicCacheProperties cacheProperties
    ) {
        this.rulingRepository = rulingRepository;
        this.rawiRepository = rawiRepository;
        this.muhaddithRepository = muhaddithRepository;
        this.bookRepository = bookRepository;
        this.topicRepository = topicRepository;
        this.meterRegistry = meterRegistry;
        this.filtersListCache = Caffeine.newBuilder()
                .maximumSize(cacheProperties.getReferenceMaxSize())
                .expireAfterWrite(cacheProperties.getReferenceExpireAfterWrite())
                .recordStats()
                .build();
    }

    public FiltersListResponseDto getFiltersList() {
        FiltersListResponseDto cached = filtersListCache.getIfPresent(FILTERS_LIST_CACHE_KEY);
        if (cached != null) {
            meterRegistry.counter("app.public_cache.requests", "cache", "filters-list", "result", "hit").increment();
            return cached;
        }

        meterRegistry.counter("app.public_cache.requests", "cache", "filters-list", "result", "miss").increment();
        FiltersListResponseDto response = new FiltersListResponseDto(
                rulingRepository.findAllRulingReferences(),
                rawiRepository.findAllRawiReferences(),
                muhaddithRepository.findAllMuhaddithReferences(),
                bookRepository.findAllBookReferences(),
                topicRepository.findAllTopicReferences());
        filtersListCache.put(FILTERS_LIST_CACHE_KEY, response);
        return response;
    }

    public void evictReferenceCaches() {
        filtersListCache.invalidateAll();
    }
}
