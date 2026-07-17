package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.features.interaction.entity.Question;

import com.jamil.ahadith.features.interaction.dto.request.QuestionRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.QuestionResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.update.QuestionUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.interaction.service.QuestionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final AdminPageService adminPageService;

    @GetMapping("/me/questions")
    public List<QuestionResponseDto> getMyQuestions() {
        return questionService.getCurrentUserQuestions();
    }

    @GetMapping({"/scholar/questions", "/admin/questions"})
    public SearchResponse<QuestionResponseDto> getQuestions(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(required = false) String sort) {
        return questionService.getQuestions(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/me/questions/{id}")
    public QuestionResponseDto getMyQuestionById(@PathVariable UUID id) {
        return questionService.getCurrentUserQuestionById(id);
    }

    @GetMapping({"/scholar/questions/{id}", "/admin/questions/{id}"})
    public QuestionResponseDto getQuestionById(@PathVariable UUID id) {
        return questionService.getQuestionById(id);
    }

    @PostMapping("/me/questions")
    public ResponseEntity<QuestionResponseDto> createQuestion(@Valid @RequestBody QuestionRequestDto request,
                                                             UriComponentsBuilder uriBuilder) {
        var question = questionService.createQuestion(request);
        var uri = uriBuilder.path("/me/questions/{id}").buildAndExpand(question.getId()).toUri();
        return ResponseEntity.created(uri).body(question);
    }

    @RequestMapping(
            value = {"/scholar/questions/{id}", "/scholar/questions/{id}/status", "/admin/questions/{id}", "/admin/questions/{id}/status"},
            method = {RequestMethod.PUT, RequestMethod.PATCH})
    public QuestionResponseDto updateQuestion(@PathVariable UUID id,
                                              @Valid @RequestBody QuestionUpdateDto request) {
        return questionService.updateQuestion(id, request);
    }

    @RequestMapping(value = "/me/questions/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public QuestionResponseDto updateMyQuestion(@PathVariable UUID id,
                                                @Valid @RequestBody QuestionUpdateDto request) {
        return questionService.updateCurrentUserQuestion(id, request);
    }

    @PostMapping("/scholar/questions/{id}/answers")
    public QuestionResponseDto answerQuestion(@PathVariable UUID id,
                                              @Valid @RequestBody QuestionUpdateDto request) {
        return questionService.updateQuestion(id, request);
    }

    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/questions/{id}")
    public ResponseEntity<Void> deleteMyQuestion(@PathVariable UUID id) {
        questionService.deleteCurrentUserQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
