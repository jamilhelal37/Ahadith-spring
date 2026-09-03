package com.jamil.ahadith.features.hadith.repository;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FakeHadithRepository extends JpaRepository<FakeHadith, UUID> {

    @Query(
            value = """
                    SELECT f.*
                    FROM fake_ahadith f
                    WHERE f.search_text LIKE '%' || public.arab_norm(:query) || '%'
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM fake_ahadith f
                    WHERE f.search_text LIKE '%' || public.arab_norm(:query) || '%'
                    """,
            nativeQuery = true
    )
    Page<FakeHadith> searchByText(
            @Param("query") String query,
            Pageable pageable
    );
}