package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Comment;
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
