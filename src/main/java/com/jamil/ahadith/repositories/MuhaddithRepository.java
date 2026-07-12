package com.jamil.ahadith.repositories;

import com.jamil.ahadith.dtos.responses.PublicBiographyDto;
import com.jamil.ahadith.entities.Muhaddith;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MuhaddithRepository extends JpaRepository<Muhaddith, UUID> {
    @Query("""
            select new com.jamil.ahadith.dtos.responses.PublicBiographyDto(0, m.id, m.name, m.about)
            from Muhaddith m
            order by m.name asc, m.id asc
            """)
    List<PublicBiographyDto> findPublicBiographies();
}
