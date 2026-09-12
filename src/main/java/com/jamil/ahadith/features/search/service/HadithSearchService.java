package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.features.search.dto.projection.HadithSearchRow;
import com.jamil.ahadith.features.search.dto.projection.HadithTopicRow;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.request.HadithSearchSort;
import com.jamil.ahadith.features.search.dto.request.SearchMode;
import com.jamil.ahadith.features.search.dto.response.AdminHadithSearchItemDto;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.search.service.SearchHistoryService;
import com.jamil.ahadith.features.search.semantic.client.EmbeddingServiceException;
import com.jamil.ahadith.features.search.semantic.service.SemanticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class HadithSearchService {
    private static final Logger log = LoggerFactory.getLogger(HadithSearchService.class);
    private static final int DEFAULT_SIZE = 20;
    private static final int DEFAULT_BOOK_AHADITH_SIZE = 50;
    private static final int MAX_SIZE = 50;
    private final SearchHistoryService searchHistoryService;

    private final HadithRepository hadithRepository;
    private final BookRepository bookRepository;
    private final SemanticSearchService semanticSearchService;

    @Transactional
    public SearchResponse<HadithSearchItemDto> publicSearch(HadithSearchRequest request) {
        HadithSearchRequest safeRequest = request == null ? new HadithSearchRequest() : request;
        String query = clean(safeRequest.getQuery());
        SearchMode mode = normalizeMode(safeRequest.getMode());
        HadithSearchSort sort = normalizeSort(safeRequest.getSort());
        int page = normalizePage(safeRequest.getPage());
        int size = normalizeSize(safeRequest.getSize());

        SearchResultIds result = switch (mode) {
            case EXACT, FLEXIBLE -> textSearch(safeRequest, query, mode, sort, page, size);
            case SEMANTIC -> semanticSearch(safeRequest, query, page, size);
            case HYBRID -> hybridSearch(safeRequest, query, sort, page, size);
        };

        List<HadithSearchItemDto> items = getHadithCardsByIdsInOrder(result.ids());

        searchHistoryService.saveCurrentUserSearch(
                query,
                SearchSource.Hadith
        );

        return new SearchResponse<>(items, result.pagination());
    }

    private SearchResultIds textSearch(HadithSearchRequest request, String query, SearchMode mode,
                                       HadithSearchSort sort, int page, int size) {
        Page<UUID> idPage = textSearchPage(request, query, mode, sort, PageRequest.of(page, size));
        return new SearchResultIds(idPage.getContent(), buildPaginationMeta(idPage));
    }

    private Page<UUID> textSearchPage(HadithSearchRequest request, String query, SearchMode mode,
                                      HadithSearchSort sort, PageRequest pageable) {
        return hadithRepository.searchPublicIds(
                query, mode.name(), sort.name(), Boolean.TRUE.equals(request.getIncludeExplanation()),
                toUuidArray(request.getMuhaddithIds()), toUuidArray(request.getRawiIds()),
                toStringArray(request.getTypes()), toUuidArray(request.getRulingIds()),
                toUuidArray(request.getBookIds()), toUuidArray(request.getTopicIds()), pageable);
    }

    private SearchResultIds semanticSearch(HadithSearchRequest request, String query, int page, int size) {
        requireSemanticQuery(query);
        int limit = semanticSearchService.candidateLimit(Math.addExact(Math.multiplyExact(page, size), size));
        List<UUID> candidates = semanticSearchService.semanticCandidates(query, request, limit);
        return paginate(candidates, page, size);
    }

    private SearchResultIds hybridSearch(HadithSearchRequest request, String query, HadithSearchSort sort,
                                         int page, int size) {
        requireSemanticQuery(query);
        if (!semanticSearchService.isEnabled()) {
            return textSearch(request, query, SearchMode.FLEXIBLE, sort, page, size);
        }
        int required = Math.addExact(Math.multiplyExact(page, size), size);
        int limit = semanticSearchService.candidateLimit(required);
        Page<UUID> textCandidates = textSearchPage(request, query, SearchMode.FLEXIBLE,
                HadithSearchSort.RELEVANCE, PageRequest.of(0, limit));
        try {
            List<UUID> semanticCandidates = semanticSearchService.semanticCandidatesForHybrid(query, request, limit);
            return paginate(semanticSearchService.fuse(textCandidates.getContent(), semanticCandidates), page, size);
        } catch (EmbeddingServiceException ex) {
            log.warn("Embedding service unavailable; falling back from HYBRID to FLEXIBLE search", ex);
            return textSearch(request, query, SearchMode.FLEXIBLE, sort, page, size);
        }
    }

    private void requireSemanticQuery(String query) {
        if (query == null) {
            throw new InvalidRequestException("query is required for semantic search");
        }
    }

    private SearchResultIds paginate(List<UUID> rankedIds, int page, int size) {
        int from = Math.min(Math.multiplyExact(page, size), rankedIds.size());
        int to = Math.min(from + size, rankedIds.size());
        long total = rankedIds.size();
        int totalPages = total == 0 ? 0 : (int) ((total + size - 1) / size);
        PaginationMeta pagination = new PaginationMeta(page, size, total, totalPages,
                page + 1 < totalPages, page > 0 && total > 0);
        return new SearchResultIds(rankedIds.subList(from, to), pagination);
    }

    private record SearchResultIds(List<UUID> ids, PaginationMeta pagination) {
    }

    public SearchResponse<HadithSearchItemDto> getBookAhadith(UUID bookId, Integer page, Integer size) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException();
        }

        int safePage = validateBookAhadithPage(page);
        int safeSize = validateBookAhadithSize(size);
        Page<UUID> idPage = hadithRepository.findBookAhadithIds(bookId, PageRequest.of(safePage, safeSize));
        List<HadithSearchItemDto> items = getHadithCardsByIdsInOrder(idPage.getContent());

        return new SearchResponse<>(items, buildPaginationMeta(idPage));
    }

    public SearchResponse<AdminHadithSearchItemDto> adminDashboardSearch(String q, int page, int size) {
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        Page<UUID> idPage = hadithRepository.searchAdminIds(clean(q), PageRequest.of(safePage, safeSize));
        Map<UUID, List<SimpleReferenceDto>> topicsByHadithId = loadSimpleTopicsByHadithIds(idPage);

        List<AdminHadithSearchItemDto> items = loadRowsInPageOrder(idPage).stream()
                .map(row -> toAdminItem(row, topicsByHadithId.getOrDefault(row.getId(), List.of())))
                .toList();

        return new SearchResponse<>(items, buildPaginationMeta(idPage));
    }

    public List<HadithSearchItemDto> getHadithCardsByIdsInOrder(List<UUID> hadithIds) {
        if (hadithIds == null || hadithIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<TopicReferenceResponseDto>> topicsByHadithId = loadPublicTopicsByHadithIds(hadithIds);
        return loadRowsInPageOrder(hadithIds).stream()
                .map(row -> toSearchItem(row, topicsByHadithId.getOrDefault(row.getId(), List.of())))
                .toList();
    }

    public String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public int normalizePage(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new InvalidRequestException("page must be greater than or equal to 0");
        }
        return page;
    }

    public int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1) {
            throw new InvalidRequestException("size must be greater than 0");
        }
        if (size > MAX_SIZE) {
            throw new InvalidRequestException("size must be less than or equal to " + MAX_SIZE);
        }
        return size;
    }

    public SearchMode normalizeMode(SearchMode mode) {
        return mode == null ? SearchMode.FLEXIBLE : mode;
    }

    public HadithSearchSort normalizeSort(HadithSearchSort sort) {
        return sort == null ? HadithSearchSort.RELEVANCE : sort;
    }

    public UUID[] toUuidArray(List<UUID> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        UUID[] filtered = values.stream()
                .filter(value -> value != null)
                .distinct()
                .toArray(UUID[]::new);
        return filtered.length == 0 ? null : filtered;
    }

    public String[] toStringArray(List<HadithType> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String[] filtered = values.stream()
                .filter(value -> value != null)
                .distinct()
                .map(Enum::name)
                .toArray(String[]::new);
        return filtered.length == 0 ? null : filtered;
    }

    public PaginationMeta buildPaginationMeta(Page<?> page) {
        return new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    private List<HadithSearchRow> loadRowsInPageOrder(Page<UUID> idPage) {
        return loadRowsInPageOrder(idPage.getContent());
    }

    private List<HadithSearchRow> loadRowsInPageOrder(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, HadithSearchRow> rowsById = hadithRepository.findSearchRowsByIdsJpa(ids).stream()
                .collect(Collectors.toMap(HadithSearchRow::getId, Function.identity()));
        return ids.stream()
                .map(rowsById::get)
                .filter(row -> row != null)
                .toList();
    }

    private Map<UUID, List<TopicReferenceResponseDto>> loadPublicTopicsByHadithIds(Page<UUID> idPage) {
        return loadPublicTopicsByHadithIds(idPage.getContent());
    }

    private Map<UUID, List<TopicReferenceResponseDto>> loadPublicTopicsByHadithIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }

        return hadithRepository.findTopicsByHadithIdsJpa(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        HadithTopicRow::getHadithId,
                        LinkedHashMap::new,
                        Collectors.mapping(row -> new TopicReferenceResponseDto(row.getId(), row.getName()), Collectors.toList())
                ));
    }

    private Map<UUID, List<SimpleReferenceDto>> loadSimpleTopicsByHadithIds(Page<UUID> idPage) {
        return loadSimpleTopicsByHadithIds(idPage.getContent());
    }

    private Map<UUID, List<SimpleReferenceDto>> loadSimpleTopicsByHadithIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }

        return hadithRepository.findTopicsByHadithIdsJpa(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        HadithTopicRow::getHadithId,
                        LinkedHashMap::new,
                        Collectors.mapping(row -> new SimpleReferenceDto(row.getId(), row.getName()), Collectors.toList())
                ));
    }

    private int validateBookAhadithPage(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new InvalidRequestException("page must be greater than or equal to 0");
        }
        return page;
    }

    private int validateBookAhadithSize(Integer size) {
        if (size == null) {
            return DEFAULT_BOOK_AHADITH_SIZE;
        }
        if (size < 1) {
            throw new InvalidRequestException("size must be greater than 0");
        }
        if (size > MAX_SIZE) {
            throw new InvalidRequestException("size must be less than or equal to " + MAX_SIZE);
        }
        return size;
    }

    private HadithSearchItemDto toSearchItem(HadithSearchRow row, List<TopicReferenceResponseDto> topics) {
        return new HadithSearchItemDto(
                row.getId(),
                row.getText(),
                row.getNormalText(),
                row.getHadithNumber(),
                row.getType(),
                row.getSanad(),
                toBookReference(row.getBookId(), row.getBookName()),
                toRawiReference(row.getRawiId(), row.getRawiName()),
                toRulingReference(row.getRulingId(), row.getRulingName()),
                toMuhaddithReference(row.getMuhaddithId(), row.getMuhaddithName()),
                topics,
                row.getExplanationId() != null,
                row.getSubValidId() != null
        );
    }

    private AdminHadithSearchItemDto toAdminItem(HadithSearchRow row, List<SimpleReferenceDto> topics) {
        return new AdminHadithSearchItemDto(
                row.getId(),
                row.getText(),
                row.getHadithNumber(),
                row.getType(),
                row.getSanad(),
                toReference(row.getBookId(), row.getBookName()),
                toReference(row.getRawiId(), row.getRawiName()),
                toReference(row.getRulingId(), row.getRulingName()),
                toReference(row.getMuhaddithId(), row.getMuhaddithName()),
                topics,
                row.getExplanationId() != null
        );
    }

    private SimpleReferenceDto toReference(UUID id, String name) {
        return id == null ? null : new SimpleReferenceDto(id, name);
    }

    private BookReferenceResponseDto toBookReference(UUID id, String name) {
        return id == null ? null : new BookReferenceResponseDto(id, name);
    }

    private RawiReferenceResponseDto toRawiReference(UUID id, String name) {
        return id == null ? null : new RawiReferenceResponseDto(id, name);
    }

    private RulingReferenceResponseDto toRulingReference(UUID id, String name) {
        return id == null ? null : new RulingReferenceResponseDto(id, name);
    }

    private MuhaddithReferenceResponseDto toMuhaddithReference(UUID id, String name) {
        return id == null ? null : new MuhaddithReferenceResponseDto(id, name);
    }


}
