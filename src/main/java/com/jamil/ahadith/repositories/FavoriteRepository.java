package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);

    Optional<Favorite> findByUserIdAndHadithId(UUID userId, UUID hadithId);
}
