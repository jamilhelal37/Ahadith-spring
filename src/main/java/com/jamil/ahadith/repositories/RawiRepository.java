package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Rawi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RawiRepository extends JpaRepository<Rawi, UUID> {
}