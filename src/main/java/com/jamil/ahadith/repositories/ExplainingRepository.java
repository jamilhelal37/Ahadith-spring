package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Explaining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExplainingRepository extends JpaRepository<Explaining, UUID> {
}