package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Ahadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AhadithRepository extends JpaRepository<Ahadith, UUID> {
}