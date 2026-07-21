package com.jamil.ahadith.features.interaction.repository;

import com.jamil.ahadith.features.interaction.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @EntityGraph(attributePaths = {
            "hadith",
            "hadith.book",
            "hadith.book.muhaddith",
            "hadith.rawi",
            "hadith.ruling",
            "asker",
            "updatedBy"
    })
    @Query("""
            select q
            from Question q
            where q.asker.id = :userId
            order by q.createdAt desc, q.id desc
            """)
    List<Question> findMemberQuestionsByAskerId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "hadith",
            "hadith.book",
            "hadith.book.muhaddith",
            "hadith.rawi",
            "hadith.ruling",
            "asker",
            "updatedBy"
    })
    @Query("""
            select q
            from Question q
            where q.id = :id
              and q.asker.id = :userId
            """)
    Optional<Question> findMemberQuestionByIdAndAskerId(@Param("id") UUID id, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "hadith",
            "hadith.book",
            "hadith.book.muhaddith",
            "hadith.rawi",
            "hadith.ruling",
            "asker",
            "updatedBy"
    })
    @Query(
            value = "select q from Question q",
            countQuery = "select count(q) from Question q"
    )
    Page<Question> findAllForScholar(Pageable pageable);

    @EntityGraph(attributePaths = {
            "hadith",
            "hadith.book",
            "hadith.book.muhaddith",
            "hadith.rawi",
            "hadith.ruling",
            "asker",
            "updatedBy"
    })
    @Query("select q from Question q where q.id = :id")
    Optional<Question> findScholarQuestionById(@Param("id") UUID id);
}
