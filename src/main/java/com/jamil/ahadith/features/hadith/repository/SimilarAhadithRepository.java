package com.jamil.ahadith.features.hadith.repository;

import com.jamil.ahadith.features.hadith.entity.SimilarAhadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimilarAhadithRepository extends JpaRepository<SimilarAhadith, UUID> {
    boolean existsByMainHadithIdAndSimHadithId(UUID mainHadithId, UUID simHadithId);

    boolean existsByMainHadithIdAndSimHadithIdAndIdNot(UUID mainHadithId, UUID simHadithId, UUID id);
}
