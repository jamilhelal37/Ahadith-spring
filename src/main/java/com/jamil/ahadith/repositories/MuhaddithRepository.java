package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Muhaddith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MuhaddithRepository extends JpaRepository<Muhaddith, UUID> {
}