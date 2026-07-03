package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.responses.HadithResponseDto;
import com.jamil.ahadith.entities.*;
import com.jamil.ahadith.mappers.HadithMapper;
import com.jamil.ahadith.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
@AllArgsConstructor
@Service
public class SearchService {
    private final HadithRepository hadithRepository;
    private final HadithMapper hadithMapper;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<HadithResponseDto> searchHadiths(String query, String generalQuery, UUID rawiId, UUID rulingId,
                                                 String hadithType, UUID topicId, UUID muhaddithId, UUID bookId,
                                                 int page, int size) {
        saveSearchHistory(query, generalQuery, rawiId, rulingId, hadithType, topicId, muhaddithId, bookId);

        List<Hadith> all = hadithRepository.findAll();

        List<Hadith> filtered = all.stream()
                .filter(hadith -> matchesQuery(hadith, query))
                .filter(hadith -> matchesGeneralQuery(hadith, generalQuery))
                .filter(hadith -> rawiId == null || Objects.equals(hadith.getRawi() != null ? hadith.getRawi().getId() : null, rawiId))
                .filter(hadith -> rulingId == null || Objects.equals(hadith.getRuling() != null ? hadith.getRuling().getId() : null, rulingId))
                .filter(hadith -> hadithType == null || hadithType.isBlank() || matchesHadithType(hadith, hadithType))
                .filter(hadith -> topicId == null || matchesTopic(hadith, topicId))
                .filter(hadith -> muhaddithId == null || matchesMuhaddith(hadith, muhaddithId))
                .filter(hadith -> bookId == null || Objects.equals(hadith.getBook() != null ? hadith.getBook().getId() : null, bookId))
                .collect(Collectors.toList());

        int fromIndex = Math.max(0, page * size);
        int toIndex = Math.min(filtered.size(), fromIndex + size);

        return filtered.subList(fromIndex, toIndex).stream()
                .map(hadithMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public List<SearchHistory> getRecentSearchHistory(int limit) {
        User user = getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return searchHistoryRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .limit(limit)
                .toList();
    }

    public List<SearchHistory> searchSearchHistory(String keyword, int limit) {
        User user = getCurrentUser();
        if (user == null) {
            return List.of();
        }
        if (keyword == null || keyword.isBlank()) {
            return getRecentSearchHistory(limit);
        }
        return searchHistoryRepository.findByUserAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(user, keyword)
                .stream()
                .limit(limit)
                .toList();
    }

    public void saveSearchHistory(String query, String generalQuery, UUID rawiId, UUID rulingId,
                                  String hadithType, UUID topicId, UUID muhaddithId, UUID bookId) {
        String combined = Stream.of(query, generalQuery, toString(rawiId), toString(rulingId), hadithType,
                        toString(topicId), toString(muhaddithId), toString(bookId))
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" | "));

        if (combined.isBlank()) {
            return;
        }

        User user = getCurrentUser();

        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setSearchText(combined);
        history.setSearchSource(SearchSource.Hadith);
        searchHistoryRepository.save(history);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private String toString(UUID value) {
        return value == null ? null : value.toString();
    }

    private boolean matchesQuery(Hadith hadith, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String haystack = String.join(" ",
                safe(hadith.getText()),
                safe(hadith.getNormalText()),
                safe(hadith.getSearchText()),
                safe(hadith.getSanad()),
                safe(hadith.getBook() != null ? hadith.getBook().getName() : null),
                safe(hadith.getRawi() != null ? hadith.getRawi().getName() : null),
                safe(hadith.getRuling() != null ? hadith.getRuling().getName() : null)
        ).toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }

    private boolean matchesGeneralQuery(Hadith hadith, String generalQuery) {
        if (generalQuery == null || generalQuery.isBlank()) {
            return true;
        }
        String haystack = String.join(" ",
                safe(hadith.getText()),
                safe(hadith.getNormalText()),
                safe(hadith.getSearchText()),
                safe(hadith.getSanad()),
                safe(hadith.getBook() != null ? hadith.getBook().getName() : null),
                safe(hadith.getRawi() != null ? hadith.getRawi().getName() : null),
                safe(hadith.getRuling() != null ? hadith.getRuling().getName() : null),
                safe(hadith.getCreatedBy() != null ? hadith.getCreatedBy().getName() : null),
                safe(hadith.getUpdatedBy() != null ? hadith.getUpdatedBy().getName() : null)
        ).toLowerCase(Locale.ROOT);
        return haystack.contains(generalQuery.toLowerCase(Locale.ROOT));
    }

    private boolean matchesHadithType(Hadith hadith, String hadithType) {
        return safe(hadith.getType()).equalsIgnoreCase(hadithType);
    }

    private boolean matchesTopic(Hadith hadith, UUID topicId) {
        return hadith.getTopicClasses() != null && hadith.getTopicClasses().stream()
                .anyMatch(topicClass -> topicClass.getTopic() != null && topicId.equals(topicClass.getTopic().getId()));
    }

    private boolean matchesMuhaddith(Hadith hadith, UUID muhaddithId) {
        return hadith.getBook() != null && hadith.getBook().getMuhaddith() != null && muhaddithId.equals(hadith.getBook().getMuhaddith().getId());
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
