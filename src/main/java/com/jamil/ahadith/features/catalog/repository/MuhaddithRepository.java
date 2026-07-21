package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.projection.PublicMuhaddithRow;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MuhaddithRepository extends JpaRepository<Muhaddith, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.projection.PublicMuhaddithRow(m.name, m.about)
            from Muhaddith m
            order by m.name asc, m.id asc
            """)
    List<PublicMuhaddithRow> findPublicMuhaddithRows();

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto(m.id, m.name)
            from Muhaddith m
            order by m.name asc, m.id asc
            """)
    List<MuhaddithReferenceResponseDto> findAllMuhaddithReferences();
}
