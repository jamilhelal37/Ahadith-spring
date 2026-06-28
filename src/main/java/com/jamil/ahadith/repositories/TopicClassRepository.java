package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.TopicClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicClassRepository extends JpaRepository<TopicClass, UUID> {
}