package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
}