package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);

    Optional<Favorite> findByUserIdAndHadithId(UUID userId, UUID hadithId);

    boolean existsByUserIdAndHadithId(UUID userId, UUID hadithId);

    Page<Favorite> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
