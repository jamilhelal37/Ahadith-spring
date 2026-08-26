package com.jamil.ahadith.features.search.repository;

import com.jamil.ahadith.features.search.entity.SearchHistory;
import com.jamil.ahadith.features.search.entity.SearchSource;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);

    long countByUser(User user);

    @Modifying
    void deleteByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query(value = """
            delete from search_history
            where id in (
                select id from search_history
                where user_id = :userId
                order by created_at asc, id asc
                limit :limit
            )
            """, nativeQuery = true)
    int deleteOldestForUser(
            @Param("userId") UUID userId,
            @Param("limit") long limit);

    List<SearchHistory> findByUserAndSearchSourceOrderByCreatedAtDesc(
            User user,
            SearchSource searchSource,
            Pageable pageable);

    List<SearchHistory> findByUserAndSearchSourceAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(
            User user,
            SearchSource searchSource,
            String searchText,
            Pageable pageable);
}