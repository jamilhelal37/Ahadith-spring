package com.jamil.ahadith.repositories;

import com.jamil.ahadith.dtos.responses.PublicBiographyDto;
import com.jamil.ahadith.entities.Rawi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RawiRepository extends JpaRepository<Rawi, UUID> {
    @Query("""
            select new com.jamil.ahadith.dtos.responses.PublicBiographyDto(0, r.id, r.name, r.about)
            from Rawi r
            order by r.name asc, r.id asc
            """)
    List<PublicBiographyDto> findPublicBiographies();
}
