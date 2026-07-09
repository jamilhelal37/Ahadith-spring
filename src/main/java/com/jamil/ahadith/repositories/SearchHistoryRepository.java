package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.SearchHistory;
import com.jamil.ahadith.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user);

    List<SearchHistory> findByUserAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(User user, String searchText);

    void deleteByUser(User user);
}
