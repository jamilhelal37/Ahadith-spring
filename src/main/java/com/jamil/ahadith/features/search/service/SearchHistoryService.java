package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.core.config.SecurityProperties;
import com.jamil.ahadith.features.search.dto.request.HadithSearchRequest;
import com.jamil.ahadith.features.search.dto.response.SearchHistoryResponseDto;
import com.jamil.ahadith.features.search.entity.SearchHistory;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.search.repository.SearchHistoryRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final CurrentUserService currentUserService;
    private final SecurityProperties securityProperties;

    public java.util.List<SearchHistoryResponseDto> getRecentSearchHistory(int limit) {
        return currentUserService.getCurrentUser()
                .map(user -> searchHistoryRepository.findByUserOrderByCreatedAtDesc(
                                user,
                                PageRequest.of(0, normalizeLimit(limit)))
                        .stream()
                        .map(this::toHistoryResponse)
                        .toList())
                .orElseGet(java.util.List::of);
    }

    public java.util.List<SearchHistoryResponseDto> searchSearchHistory(String keyword, int limit) {
        return currentUserService.getCurrentUser()
                .map(user -> searchUserHistory(user, keyword, limit))
                .orElseGet(java.util.List::of);
    }

    @Transactional
    public void deleteCurrentUserSearchHistory() {
        currentUserService.getCurrentUser()
                .ifPresent(searchHistoryRepository::deleteByUser);
    }

    @Transactional
    public void deleteCurrentUserSearchHistoryItem(UUID id) {
        currentUserService.getCurrentUser()
                .ifPresent(user -> searchHistoryRepository.deleteByIdAndUserId(id, user.getId()));
    }

    @Transactional
    public void saveCurrentUserHadithSearch(HadithSearchRequest request) {
        currentUserService.getCurrentUser()
                .ifPresent(user -> saveHadithSearch(user, request));
    }

    @Transactional
    public void pruneHistory(User user) {
        if (user == null) {
            return;
        }
        long excess = searchHistoryRepository.countByUser(user)
                - securityProperties.getSearchHistoryMaxPerUser();
        if (excess > 0) {
            searchHistoryRepository.deleteOldestForUser(user.getId(), excess);
        }
    }

    private java.util.List<SearchHistoryResponseDto> searchUserHistory(User user, String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return getRecentSearchHistory(limit);
        }
        return searchHistoryRepository.findByUserAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(
                        user,
                        keyword,
                        PageRequest.of(0, normalizeLimit(limit)))
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void saveHadithSearch(User user, HadithSearchRequest request) {
        String searchText = toSearchText(request);
        if (searchText.isBlank()) {
            return;
        }

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setSearchText(searchText);
        history.setSearchSource(SearchSource.Hadith);
        searchHistoryRepository.save(history);
        pruneHistory(user);
    }

    private String toSearchText(HadithSearchRequest request) {
        HadithSearchRequest safeRequest = request == null ? new HadithSearchRequest() : request;
        return Stream.of(
                        clean(safeRequest.getQuery()),
                        safeRequest.getMode() == null ? null : safeRequest.getMode().name(),
                        safeRequest.getSort() == null ? null : safeRequest.getSort().name(),
                        Boolean.TRUE.equals(safeRequest.getIncludeExplanation()) ? "includeExplanation=true" : null,
                        join(safeRequest.getMuhaddithIds()),
                        join(safeRequest.getRawiIds()),
                        join(safeRequest.getTypes()),
                        join(safeRequest.getRulingIds()),
                        join(safeRequest.getBookIds()),
                        join(safeRequest.getTopicIds()))
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" | "));
    }

    private String join(Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String joined = values.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(","));
        return joined.isBlank() ? null : joined;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, securityProperties.getSearchHistoryMaxPerUser());
    }

    private SearchHistoryResponseDto toHistoryResponse(SearchHistory history) {
        return new SearchHistoryResponseDto(
                history.getId(),
                history.getSearchText(),
                history.getSearchSource(),
                history.getCreatedAt());
    }
}
