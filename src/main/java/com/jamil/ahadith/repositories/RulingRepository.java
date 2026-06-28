package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Ruling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RulingRepository extends JpaRepository<Ruling, UUID> {
}