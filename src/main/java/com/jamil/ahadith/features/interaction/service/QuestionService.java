package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.interaction.entity.Question;

import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.interaction.dto.request.QuestionRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.QuestionResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.update.QuestionUpdateDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.interaction.exception.QuestionNotFoundException;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.interaction.mapper.QuestionMapper;
import com.jamil.ahadith.features.interaction.repository.QuestionRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final HadithRepository hadithRepository;
    private final AdminPageService adminPageService;

    public SearchResponse<QuestionResponseDto> getQuestions(Pageable pageable) {
        return adminPageService.response(questionRepository.findAll(pageable).map(questionMapper::toResponseDto));
    }

    public List<QuestionResponseDto> getCurrentUserQuestions() {
        User user = currentUserService.requireCurrentUser();
        return questionRepository.findByAskerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(questionMapper::toResponseDto)
                .toList();
    }

    public QuestionResponseDto getQuestionById(UUID id) {
        return questionRepository.findById(id)
                .map(questionMapper::toResponseDto)
                .orElseThrow(QuestionNotFoundException::new);
    }

    public QuestionResponseDto getCurrentUserQuestionById(UUID id) {
        User user = currentUserService.requireCurrentUser();
        return questionRepository.findByIdAndAskerId(id, user.getId())
                .map(questionMapper::toResponseDto)
                .orElseThrow(QuestionNotFoundException::new);
    }

    public QuestionResponseDto createQuestion(QuestionRequestDto request) {
        var question = questionMapper.toEntity(request);
        question.setAsker(currentUserService.requireCurrentUser());
        question.setIsActive(false);
        if (request.getHadithId() != null) {
            question.setHadith(hadithRepository.findById(request.getHadithId())
                    .orElseThrow(com.jamil.ahadith.features.hadith.exception.HadithNotFoundException::new));
        }
        question = questionRepository.saveAndFlush(question);
        entityManager.refresh(question);
        return questionMapper.toResponseDto(question);
    }

    public QuestionResponseDto updateQuestion(UUID id, QuestionUpdateDto request) {
        var question = questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
        questionMapper.updateEntity(request, question);
        if (request.getHadithId() != null) {
            question.setHadith(hadithRepository.findById(request.getHadithId())
                    .orElseThrow(com.jamil.ahadith.features.hadith.exception.HadithNotFoundException::new));
        }
        currentUserService.getCurrentUser().ifPresent(question::setUpdatedBy);
        var savedQuestion = questionRepository.saveAndFlush(question);
        entityManager.refresh(savedQuestion);
        return questionMapper.toResponseDto(savedQuestion);
    }

    public QuestionResponseDto updateCurrentUserQuestion(UUID id, QuestionUpdateDto request) {
        User user = currentUserService.requireCurrentUser();
        var question = questionRepository.findByIdAndAskerId(id, user.getId()).orElseThrow(QuestionNotFoundException::new);
        if (request.getAskerText() != null) {
            question.setAskerText(request.getAskerText());
        }
        if (request.getHadithId() != null) {
            question.setHadith(hadithRepository.findById(request.getHadithId())
                    .orElseThrow(com.jamil.ahadith.features.hadith.exception.HadithNotFoundException::new));
        }
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

    public void deleteCurrentUserQuestion(UUID id) {
        User user = currentUserService.requireCurrentUser();
        var question = questionRepository.findByIdAndAskerId(id, user.getId()).orElseThrow(QuestionNotFoundException::new);
        questionRepository.delete(question);
    }
}
