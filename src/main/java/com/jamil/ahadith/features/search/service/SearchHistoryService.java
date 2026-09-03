package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.core.config.SecurityProperties;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final CurrentUserService currentUserService;
    private final SecurityProperties securityProperties;

    public List<SearchHistoryResponseDto> getRecentSearchHistory(
            SearchSource source,
            int limit) {

        return currentUserService.getCurrentUser()
                .map(user -> searchHistoryRepository
                        .findByUserAndSearchSourceOrderByCreatedAtDesc(
                                user,
                                source,
                                PageRequest.of(0, normalizeLimit(limit)))
                        .stream()
                        .map(this::toHistoryResponse)
                        .toList())
                .orElseGet(List::of);
    }

    public List<SearchHistoryResponseDto> searchSearchHistory(
            SearchSource source,
            String keyword,
            int limit) {

        return currentUserService.getCurrentUser()
                .map(user -> searchUserHistory(user, source, keyword, limit))
                .orElseGet(List::of);
    }

    @Transactional
    public void saveCurrentUserSearch(String query, SearchSource source) {

        String searchText = clean(query);

        if (searchText == null || searchText.isBlank()) {
            return;
        }

        currentUserService.getCurrentUser()
                .ifPresent(user -> {

                    SearchHistory history = new SearchHistory();

                    history.setUser(user);
                    history.setSearchText(searchText);
                    history.setSearchSource(source);

                    searchHistoryRepository.save(history);

                    pruneHistory(user);
                });
    }

    @Transactional
    public void deleteCurrentUserSearchHistory() {

        currentUserService.getCurrentUser()
                .ifPresent(searchHistoryRepository::deleteByUser);
    }

    @Transactional
    public void deleteCurrentUserSearchHistoryItem(UUID id) {

        currentUserService.getCurrentUser()
                .ifPresent(user ->
                        searchHistoryRepository.deleteByIdAndUserId(
                                id,
                                user.getId()));
    }

    public void pruneHistory(User user) {

        if (user == null) {
            return;
        }

        long excess = searchHistoryRepository.countByUser(user)
                - securityProperties.getSearchHistoryMaxPerUser();

        if (excess > 0) {
            searchHistoryRepository.deleteOldestForUser(
                    user.getId(),
                    excess);
        }
    }

    private List<SearchHistoryResponseDto> searchUserHistory(
            User user,
            SearchSource source,
            String keyword,
            int limit) {

        if (keyword == null || keyword.isBlank()) {

            return searchHistoryRepository
                    .findByUserAndSearchSourceOrderByCreatedAtDesc(
                            user,
                            source,
                            PageRequest.of(0, normalizeLimit(limit)))
                    .stream()
                    .map(this::toHistoryResponse)
                    .toList();
        }

        return searchHistoryRepository
                .findByUserAndSearchSourceAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(
                        user,
                        source,
                        keyword.trim(),
                        PageRequest.of(0, normalizeLimit(limit)))
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private int normalizeLimit(int limit) {

        if (limit < 1) {
            return 5;
        }

        return Math.min(
                limit,
                securityProperties.getSearchHistoryMaxPerUser());
    }

    private SearchHistoryResponseDto toHistoryResponse(
            SearchHistory history) {

        return new SearchHistoryResponseDto(
                history.getId(),
                history.getSearchText(),
                history.getSearchSource(),
                history.getCreatedAt());
    }
}