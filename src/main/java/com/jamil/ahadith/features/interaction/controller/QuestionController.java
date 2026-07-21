package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.request.QuestionAnswerRequestDto;
import com.jamil.ahadith.features.interaction.dto.request.QuestionCreateRequestDto;
import com.jamil.ahadith.features.interaction.dto.request.QuestionStatusUpdateRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.MemberQuestionResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.ScholarQuestionResponseDto;
import com.jamil.ahadith.features.interaction.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping({"/me/questions", "/api/v1/me/questions"})
    public ResponseEntity<MemberQuestionResponseDto> createQuestion(@Valid @RequestBody QuestionCreateRequestDto request,
                                                                    UriComponentsBuilder uriBuilder) {
        MemberQuestionResponseDto question = questionService.createQuestion(request);
        var uri = uriBuilder.path("/api/v1/me/questions/{id}").buildAndExpand(question.getId()).toUri();
        return ResponseEntity.created(uri).body(question);
    }

    @GetMapping({"/me/questions", "/api/v1/me/questions"})
    public List<MemberQuestionResponseDto> getMyQuestions() {
        return questionService.getCurrentUserQuestions();
    }

    @GetMapping({"/me/questions/{id}", "/api/v1/me/questions/{id}"})
    public MemberQuestionResponseDto getMyQuestionById(@PathVariable UUID id) {
        return questionService.getCurrentUserQuestionById(id);
    }

    @DeleteMapping({"/me/questions/{id}", "/api/v1/me/questions/{id}"})
    public ResponseEntity<Void> deleteMyQuestion(@PathVariable UUID id) {
        questionService.deleteCurrentUserQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/scholar/questions", "/admin/questions", "/api/v1/scholar/questions", "/api/v1/admin/questions"})
    public SearchResponse<ScholarQuestionResponseDto> getQuestions(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size,
                                                                   @RequestParam(required = false) String sort) {
        return questionService.getQuestions(page, size, sort);
    }

    @GetMapping({"/scholar/questions/{id}", "/admin/questions/{id}", "/api/v1/scholar/questions/{id}", "/api/v1/admin/questions/{id}"})
    public ScholarQuestionResponseDto getQuestionById(@PathVariable UUID id) {
        return questionService.getQuestionById(id);
    }

    @PatchMapping({"/scholar/questions/{id}/answer", "/admin/questions/{id}/answer", "/api/v1/scholar/questions/{id}/answer", "/api/v1/admin/questions/{id}/answer"})
    public ScholarQuestionResponseDto answerQuestion(@PathVariable UUID id,
                                                     @Valid @RequestBody QuestionAnswerRequestDto request) {
        return questionService.answerQuestion(id, request);
    }

    @PatchMapping({"/scholar/questions/{id}/status", "/admin/questions/{id}/status", "/api/v1/scholar/questions/{id}/status", "/api/v1/admin/questions/{id}/status"})
    public ScholarQuestionResponseDto updateQuestionStatus(@PathVariable UUID id,
                                                           @Valid @RequestBody QuestionStatusUpdateRequestDto request) {
        return questionService.updateQuestionStatus(id, request);
    }

    @DeleteMapping({"/admin/questions/{id}", "/api/v1/admin/questions/{id}"})
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
