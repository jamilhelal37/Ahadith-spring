package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Optional<Favorite> findByUserIdAndHadithId(UUID userId, UUID hadithId);

    boolean existsByUserIdAndHadithId(UUID userId, UUID hadithId);

    @Query(
            value = """
                    select f.hadith.id
                    from Favorite f
                    where f.user.id = :userId
                    order by f.createdAt desc, f.id desc
                    """,
            countQuery = """
                    select count(f.id)
                    from Favorite f
                    where f.user.id = :userId
                    """
    )
    Page<UUID> findFavoriteHadithIdsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
