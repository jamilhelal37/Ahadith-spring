package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByAskerIdOrderByCreatedAtDesc(UUID userId);

    Optional<Question> findByIdAndAskerId(UUID id, UUID userId);

    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
