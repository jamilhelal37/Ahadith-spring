package com.jamil.ahadith.features.hadith.repository;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FakeHadithRepository extends JpaRepository<FakeHadith, UUID> {
}