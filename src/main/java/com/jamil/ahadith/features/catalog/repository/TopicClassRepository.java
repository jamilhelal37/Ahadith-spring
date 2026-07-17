package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.entity.TopicClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicClassRepository extends JpaRepository<TopicClass, UUID> {
}