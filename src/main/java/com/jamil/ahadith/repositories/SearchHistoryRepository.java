package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
}