package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.QuestionRequestDto;
import com.jamil.ahadith.dtos.responses.QuestionResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.QuestionUpdateDto;
import com.jamil.ahadith.entities.User;
import com.jamil.ahadith.exceptions.QuestionNotFoundException;
import com.jamil.ahadith.repositories.HadithRepository;
import com.jamil.ahadith.mappers.QuestionMapper;
import com.jamil.ahadith.repositories.QuestionRepository;
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
                    .orElseThrow(com.jamil.ahadith.exceptions.HadithNotFoundException::new));
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
                    .orElseThrow(com.jamil.ahadith.exceptions.HadithNotFoundException::new));
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
                    .orElseThrow(com.jamil.ahadith.exceptions.HadithNotFoundException::new));
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
