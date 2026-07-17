package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.entity.Ruling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RulingRepository extends JpaRepository<Ruling, UUID> {
}