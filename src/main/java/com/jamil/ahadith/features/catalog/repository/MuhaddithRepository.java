package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.response.PublicBiographyDto;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MuhaddithRepository extends JpaRepository<Muhaddith, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.PublicBiographyDto(0, m.id, m.name, m.about)
            from Muhaddith m
            order by m.name asc, m.id asc
            """)
    List<PublicBiographyDto> findPublicBiographies();
}
