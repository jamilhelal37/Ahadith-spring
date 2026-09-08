package com.jamil.ahadith.features.hadith.repository;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FakeHadithRepository extends JpaRepository<FakeHadith, UUID> {

    @Query("""
            select f
            from FakeHadith f
            where f.searchText like concat(
                '%',
                cast(function('arab_norm', :query) as string),
                '%'
            )
            """)
    Page<FakeHadith> searchByText(
            @Param("query") String query,
            Pageable pageable
    );
}