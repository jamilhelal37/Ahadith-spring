package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);

    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
