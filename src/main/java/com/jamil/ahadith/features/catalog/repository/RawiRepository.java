package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.projection.PublicRawiRow;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RawiRepository extends JpaRepository<Rawi, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.projection.PublicRawiRow(r.name, r.about)
            from Rawi r
            order by r.name asc, r.id asc
            """)
    List<PublicRawiRow> findPublicRawiRows();

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto(r.id, r.name)
            from Rawi r
            order by r.name asc, r.id asc
            """)
    List<RawiReferenceResponseDto> findAllRawiReferences();
}
