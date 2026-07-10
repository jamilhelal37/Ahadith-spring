package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.SearchHistory;
import com.jamil.ahadith.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user);

    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<SearchHistory> findByUserAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(User user, String searchText);

    List<SearchHistory> findByUserAndSearchTextContainingIgnoreCaseOrderByCreatedAtDesc(User user, String searchText, Pageable pageable);

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
    int deleteOldestForUser(@Param("userId") UUID userId, @Param("limit") long limit);
}
