package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.interaction.dto.request.QuestionAnswerRequestDto;
import com.jamil.ahadith.features.interaction.dto.request.QuestionCreateRequestDto;
import com.jamil.ahadith.features.interaction.dto.request.QuestionStatusUpdateRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.MemberQuestionResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.ScholarQuestionResponseDto;
import com.jamil.ahadith.features.interaction.entity.Question;
import com.jamil.ahadith.features.interaction.event.QuestionActivatedEvent;
import com.jamil.ahadith.features.interaction.exception.QuestionNotFoundException;
import com.jamil.ahadith.features.interaction.mapper.QuestionMapper;
import com.jamil.ahadith.features.interaction.repository.QuestionRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private static final Set<String> SCHOLAR_QUESTION_SORTS =
            Set.of("createdAt", "updatedAt", "id");

    private static final Sort DEFAULT_SCHOLAR_SORT =
            Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.DESC, "id"));

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final HadithRepository hadithRepository;
    private final AdminPageService adminPageService;
    private final AuditEventPublisher auditEventPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public SearchResponse<ScholarQuestionResponseDto> getQuestions(
            int page,
            int size,
            String sort
    ) {
        var pageable = adminPageService.pageable(
                page,
                size,
                sort,
                SCHOLAR_QUESTION_SORTS,
                DEFAULT_SCHOLAR_SORT
        );

        return adminPageService.response(
                questionRepository.findAllForScholar(pageable)
                        .map(questionMapper::toScholarResponseDto)
        );
    }

    @Transactional(readOnly = true)
    public List<MemberQuestionResponseDto> getCurrentUserQuestions() {
        User user = currentUserService.requireCurrentUser();

        return questionRepository
                .findMemberQuestionsByAskerId(user.getId())
                .stream()
                .map(questionMapper::toMemberResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScholarQuestionResponseDto getQuestionById(UUID id) {
        return questionRepository
                .findScholarQuestionById(id)
                .map(questionMapper::toScholarResponseDto)
                .orElseThrow(QuestionNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MemberQuestionResponseDto getCurrentUserQuestionById(UUID id) {
        User user = currentUserService.requireCurrentUser();

        return questionRepository
                .findMemberQuestionByIdAndAskerId(
                        id,
                        user.getId()
                )
                .map(questionMapper::toMemberResponseDto)
                .orElseThrow(QuestionNotFoundException::new);
    }

    public MemberQuestionResponseDto createQuestion(
            QuestionCreateRequestDto request
    ) {
        Question question =
                questionMapper.toEntity(request);

        question.setAsker(
                currentUserService.requireCurrentUser()
        );

        question.setIsActive(false);

        if (request.getHadithId() != null) {
            question.setHadith(
                    hadithRepository
                            .findById(request.getHadithId())
                            .orElseThrow(
                                    HadithNotFoundException::new
                            )
            );
        }

        Question savedQuestion =
                questionRepository.saveAndFlush(question);

        entityManager.refresh(savedQuestion);

        return questionMapper
                .toMemberResponseDto(savedQuestion);
    }

    public ScholarQuestionResponseDto answerQuestion(
            UUID id,
            QuestionAnswerRequestDto request
    ) {
        Question question =
                questionRepository
                        .findScholarQuestionById(id)
                        .orElseThrow(
                                QuestionNotFoundException::new
                        );

        var oldData =
                AuditData.snapshot(question);

        question.setAnswerText(
                request.getAnswerText()
        );

        question.setUpdatedBy(
                currentUserService.requireCurrentUser()
        );

        Question savedQuestion =
                questionRepository.saveAndFlush(question);

        entityManager.refresh(savedQuestion);

        auditEventPublisher.publishUpdate(
                "questions",
                savedQuestion.getId(),
                oldData,
                AuditData.snapshot(savedQuestion)
        );

        return questionMapper
                .toScholarResponseDto(savedQuestion);
    }

    public ScholarQuestionResponseDto updateQuestionStatus(
            UUID id,
            QuestionStatusUpdateRequestDto request
    ) {
        Question question =
                questionRepository
                        .findScholarQuestionById(id)
                        .orElseThrow(
                                QuestionNotFoundException::new
                        );

        boolean wasActive =
                Boolean.TRUE.equals(
                        question.getIsActive()
                );

        var oldData =
                AuditData.snapshot(question);

        if (Boolean.TRUE.equals(
                request.getIsActive()
        ) && isBlank(question.getAnswerText())) {

            throw new InvalidRequestException(
                    "answerText is required before activating question"
            );
        }

        question.setIsActive(
                request.getIsActive()
        );

        question.setUpdatedBy(
                currentUserService.requireCurrentUser()
        );

        Question savedQuestion =
                questionRepository.saveAndFlush(question);

        entityManager.refresh(savedQuestion);

        auditEventPublisher.publishUpdate(
                "questions",
                savedQuestion.getId(),
                oldData,
                AuditData.snapshot(savedQuestion)
        );

        boolean isNowActive =
                Boolean.TRUE.equals(
                        savedQuestion.getIsActive()
                );

        if (!wasActive && isNowActive) {
            eventPublisher.publishEvent(
                    new QuestionActivatedEvent(
                            savedQuestion
                                    .getAsker()
                                    .getId(),
                            savedQuestion.getId()
                    )
            );
        }

        return questionMapper
                .toScholarResponseDto(savedQuestion);
    }

    public void deleteQuestion(UUID id) {
        Question question =
                questionRepository
                        .findById(id)
                        .orElseThrow(
                                QuestionNotFoundException::new
                        );

        var oldData =
                AuditData.snapshot(question);

        questionRepository.delete(question);

        auditEventPublisher.publishDelete(
                "questions",
                id,
                oldData
        );
    }

    public void deleteCurrentUserQuestion(UUID id) {
        User user =
                currentUserService.requireCurrentUser();

        Question question =
                questionRepository
                        .findMemberQuestionByIdAndAskerId(
                                id,
                                user.getId()
                        )
                        .orElseThrow(
                                QuestionNotFoundException::new
                        );

        questionRepository.delete(question);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}