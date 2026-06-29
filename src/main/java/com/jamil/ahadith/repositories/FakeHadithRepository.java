package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.FakeHadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FakeHadithRepository extends JpaRepository<FakeHadith, UUID> {
}