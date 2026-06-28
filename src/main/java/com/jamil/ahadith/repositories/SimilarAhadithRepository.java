package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.SimilarAhadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimilarAhadithRepository extends JpaRepository<SimilarAhadith, UUID> {
}