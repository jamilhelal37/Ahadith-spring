package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.response.PublicBiographyDto;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RawiRepository extends JpaRepository<Rawi, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.PublicBiographyDto(0, r.id, r.name, r.about)
            from Rawi r
            order by r.name asc, r.id asc
            """)
    List<PublicBiographyDto> findPublicBiographies();
}
