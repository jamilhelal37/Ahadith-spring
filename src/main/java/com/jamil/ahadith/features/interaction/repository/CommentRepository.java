package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @EntityGraph(attributePaths = {"user", "hadith", "hadith.book"})
    Page<Comment> findByHadithId(UUID hadithId, Pageable pageable);

    @EntityGraph(attributePaths = {"hadith", "hadith.book"})
    Page<Comment> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"hadith", "hadith.book"})
    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"user", "hadith", "hadith.book"})
    Page<Comment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "hadith", "hadith.book"})
    Optional<Comment> findById(UUID id);

    long countByHadithId(UUID hadithId);
}
