package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RulingRepository extends JpaRepository<Ruling, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto(r.id, r.name)
            from Ruling r
            order by r.name asc, r.id asc
            """)
    List<RulingReferenceResponseDto> findAllRulingReferences();
}
