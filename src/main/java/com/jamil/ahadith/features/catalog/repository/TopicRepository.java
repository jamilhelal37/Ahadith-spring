package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
}