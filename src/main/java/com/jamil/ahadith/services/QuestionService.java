package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.QuestionRequestDto;
import com.jamil.ahadith.dtos.responses.QuestionResponseDto;
import com.jamil.ahadith.dtos.updates.QuestionUpdateDto;
import com.jamil.ahadith.exceptions.QuestionNotFoundException;
import com.jamil.ahadith.mappers.QuestionMapper;
import com.jamil.ahadith.repositories.QuestionRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final EntityManager entityManager;

    public List<QuestionResponseDto> getQuestions() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toResponseDto)
                .toList();
    }

    public QuestionResponseDto getQuestionById(UUID id) {
        return questionRepository.findById(id)
                .map(questionMapper::toResponseDto)
                .orElseThrow(QuestionNotFoundException::new);
    }

    public QuestionResponseDto createQuestion(QuestionRequestDto request) {
        var question = questionRepository.saveAndFlush(questionMapper.toEntity(request));
        entityManager.refresh(question);
        return questionMapper.toResponseDto(question);
    }

    public QuestionResponseDto updateQuestion(UUID id, QuestionUpdateDto request) {
        var question = questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
        questionMapper.updateEntity(request, question);
        var savedQuestion = questionRepository.saveAndFlush(question);
        entityManager.refresh(savedQuestion);
        return questionMapper.toResponseDto(savedQuestion);
    }

    public void deleteQuestion(UUID id) {
        if (!questionRepository.existsById(id)) {
            throw new QuestionNotFoundException();
        }
        questionRepository.deleteById(id);
    }
}
