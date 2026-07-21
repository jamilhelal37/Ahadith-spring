package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto(t.id, t.name)
            from Topic t
            order by t.name asc, t.id asc
            """)
    List<TopicReferenceResponseDto> findAllTopicReferences();
}
