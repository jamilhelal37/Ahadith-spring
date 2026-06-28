package com.jamil.ahadith.repositories;

import com.jamil.ahadith.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}