package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.search.dto.projection.HadithSearchRow;
import com.jamil.ahadith.features.search.dto.projection.HadithTopicRow;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.request.HadithSearchSort;
import com.jamil.ahadith.features.search.dto.request.SearchMode;
import com.jamil.ahadith.features.search.dto.response.AdminHadithSearchItemDto;
import com.jamil.ahadith.features.search.dto.response.BookFilterOptionDto;
import com.jamil.ahadith.features.search.dto.response.ExplanationDto;
import com.jamil.ahadith.features.search.dto.response.HadithDetailsDto;
import com.jamil.ahadith.features.search.dto.response.HadithFiltersDto;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.search.dto.response.TypeOptionDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private static final int DEFAULT_SIZE = 20;
    private static final int DEFAULT_BOOK_AHADITH_SIZE = 50;
    private static final int MAX_SIZE = 50;

    private final HadithRepository hadithRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final BookRepository bookRepository;
    private final TopicRepository topicRepository;

    public SearchResponse<HadithSearchItemDto> publicSearch(HadithSearchRequest request) {
        HadithSearchRequest safeRequest = request == null ? new HadithSearchRequest() : request;
        String query = clean(safeRequest.getQuery());
        SearchMode mode = normalizeMode(safeRequest.getMode());
        HadithSearchSort sort = normalizeSort(safeRequest.getSort());
        int page = normalizePage(safeRequest.getPage());
        int size = normalizeSize(safeRequest.getSize());

        Page<UUID> idPage = hadithRepository.searchPublicIds(
                query,
                mode.name(),
                sort.name(),
                Boolean.TRUE.equals(safeRequest.getIncludeExplanation()),
                toUuidArray(safeRequest.getMuhaddithIds()),
                toUuidArray(safeRequest.getRawiIds()),
                toStringArray(safeRequest.getTypes()),
                toUuidArray(safeRequest.getRulingIds()),
                toUuidArray(safeRequest.getBookIds()),
                toUuidArray(safeRequest.getTopicIds()),
                PageRequest.of(page, size)
        );

        Map<UUID, List<SimpleReferenceDto>> topicsByHadithId = loadTopicsByHadithIds(idPage);
        List<HadithSearchItemDto> items = loadRowsInPageOrder(idPage).stream()
                .map(row -> toSearchItem(row, topicsByHadithId.getOrDefault(row.getId(), List.of())))
                .toList();

        return new SearchResponse<>(items, buildPaginationMeta(idPage));
    }

    public HadithFiltersDto getFilters() {
        return new HadithFiltersDto(
                muhaddithRepository.findAll().stream()
                        .sorted(Comparator.comparing(item -> safe(item.getName())))
                        .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                        .toList(),
                rawiRepository.findAll().stream()
                        .sorted(Comparator.comparing(item -> safe(item.getName())))
                        .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                        .toList(),
                List.of(
                        new TypeOptionDto(HadithType.marfu.name(), "مرفوع"),
                        new TypeOptionDto(HadithType.mawquf.name(), "موقوف"),
                        new TypeOptionDto(HadithType.qudsi.name(), "قدسي"),
                        new TypeOptionDto(HadithType.atharSahaba.name(), "أثر صحابي")
                ),
                rulingRepository.findAll().stream()
                        .sorted(Comparator.comparing(item -> safe(item.getName())))
                        .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                        .toList(),
                bookRepository.findAll().stream()
                        .sorted(Comparator.comparing(item -> safe(item.getName())))
                        .map(this::toBookFilterOption)
                        .toList(),
                topicRepository.findAll().stream()
                        .sorted(Comparator.comparing(item -> safe(item.getName())))
                        .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                        .toList()
        );
    }

    public HadithDetailsDto getDetails(UUID id) {
        HadithSearchRow row = hadithRepository.findSearchRowById(id);
        if (row == null) {
            throw new HadithNotFoundException();
        }

        Map<UUID, List<SimpleReferenceDto>> topicsByHadithId = loadTopicsByHadithIds(List.of(id));
        return toDetails(row, topicsByHadithId.getOrDefault(id, List.of()));
    }

    public SearchResponse<HadithSearchItemDto> getBookAhadith(UUID bookId, Integer page, Integer size) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException();
        }

        int safePage = validateBookAhadithPage(page);
        int safeSize = validateBookAhadithSize(size);
        Page<Hadith> hadithPage = hadithRepository.findBookAhadithPage(bookId, PageRequest.of(safePage, safeSize));
        List<UUID> ids = hadithPage.getContent().stream()
                .map(Hadith::getId)
                .toList();
        Map<UUID, List<SimpleReferenceDto>> topicsByHadithId = loadTopicsByHadithIds(ids);
        List<HadithSearchItemDto> items = hadithPage.getContent().stream()
                .map(hadith -> toSearchItem(hadith, topicsByHadithId.getOrDefault(hadith.getId(), List.of())))
                .toList();

        return new SearchResponse<>(items, buildPaginationMeta(hadithPage));
    }

    public SearchResponse<AdminHadithSearchItemDto> adminDashboardSearch(String q, int page, int size) {
        int safePage = normalizePage(page);
        int safeSize = normalizeSize(size);
        Page<UUID> idPage = hadithRepository.searchAdminIds(clean(q), PageRequest.of(safePage, safeSize));
        Map<UUID, List<SimpleReferenceDto>> topicsByHadithId = loadTopicsByHadithIds(idPage);

        List<AdminHadithSearchItemDto> items = loadRowsInPageOrder(idPage).stream()
                .map(row -> toAdminItem(row, topicsByHadithId.getOrDefault(row.getId(), List.of())))
                .toList();

        return new SearchResponse<>(items, buildPaginationMeta(idPage));
    }

    public String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public int normalizePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    public int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
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
        return values.toArray(UUID[]::new);
    }

    public String[] toStringArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String[] filtered = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
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
        Map<UUID, HadithSearchRow> rowsById = hadithRepository.findSearchRowsByIds(ids.toArray(UUID[]::new)).stream()
                .collect(Collectors.toMap(HadithSearchRow::getId, Function.identity()));
        return ids.stream()
                .map(rowsById::get)
                .filter(row -> row != null)
                .toList();
    }

    private Map<UUID, List<SimpleReferenceDto>> loadTopicsByHadithIds(Page<UUID> idPage) {
        return loadTopicsByHadithIds(idPage.getContent());
    }

    private Map<UUID, List<SimpleReferenceDto>> loadTopicsByHadithIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<SimpleReferenceDto>> grouped = hadithRepository.findTopicsByHadithIdsJpa(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        HadithTopicRow::getHadithId,
                        LinkedHashMap::new,
                        Collectors.mapping(row -> new SimpleReferenceDto(row.getId(), row.getName()), Collectors.toList())
                ));
        return grouped;
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
        return Math.min(size, MAX_SIZE);
    }

    private HadithSearchItemDto toSearchItem(HadithSearchRow row, List<SimpleReferenceDto> topics) {
        return new HadithSearchItemDto(
                row.getId(),
                row.getText(),
                row.getHadithNumber(),
                row.getType(),
                toReference(row.getBookId(), row.getBookName()),
                toReference(row.getRawiId(), row.getRawiName()),
                toReference(row.getRulingId(), row.getRulingName()),
                toReference(row.getMuhaddithId(), row.getMuhaddithName()),
                topics,
                row.getExplanationId() != null
        );
    }

    private HadithSearchItemDto toSearchItem(Hadith hadith, List<SimpleReferenceDto> topics) {
        Book book = hadith.getBook();
        return new HadithSearchItemDto(
                hadith.getId(),
                hadith.getText(),
                hadith.getHadithNumber(),
                hadith.getType() == null ? null : hadith.getType().toString(),
                book == null ? null : toReference(book.getId(), book.getName()),
                hadith.getRawi() == null ? null : toReference(hadith.getRawi().getId(), hadith.getRawi().getName()),
                hadith.getRuling() == null ? null : toReference(hadith.getRuling().getId(), hadith.getRuling().getName()),
                book == null || book.getMuhaddith() == null
                        ? null
                        : toReference(book.getMuhaddith().getId(), book.getMuhaddith().getName()),
                topics,
                hadith.getExplaining() != null
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

    private HadithDetailsDto toDetails(HadithSearchRow row, List<SimpleReferenceDto> topics) {
        return new HadithDetailsDto(
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
                row.getExplanationId() == null
                        ? null
                        : new ExplanationDto(row.getExplanationId(), row.getExplanationText())
        );
    }

    private BookFilterOptionDto toBookFilterOption(Book book) {
        SimpleReferenceDto muhaddith = book.getMuhaddith() == null
                ? null
                : new SimpleReferenceDto(book.getMuhaddith().getId(), book.getMuhaddith().getName());
        return new BookFilterOptionDto(book.getId(), book.getName(), muhaddith);
    }

    private SimpleReferenceDto toReference(UUID id, String name) {
        return id == null ? null : new SimpleReferenceDto(id, name);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
