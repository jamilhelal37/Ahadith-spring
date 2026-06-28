package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.FakeAhadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FakeAhadithRepository extends JpaRepository<FakeAhadith, UUID> {
}