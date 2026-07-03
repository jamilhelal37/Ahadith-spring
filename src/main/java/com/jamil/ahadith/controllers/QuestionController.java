package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.QuestionRequestDto;
import com.jamil.ahadith.dtos.responses.QuestionResponseDto;
import com.jamil.ahadith.dtos.updates.QuestionUpdateDto;
import com.jamil.ahadith.services.QuestionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public List<QuestionResponseDto> getQuestions() {
        return questionService.getQuestions();
    }

    @GetMapping("/{id}")
    public QuestionResponseDto getQuestionById(@PathVariable UUID id) {
        return questionService.getQuestionById(id);
    }

    @PostMapping
    public ResponseEntity<QuestionResponseDto> createQuestion(@Valid @RequestBody QuestionRequestDto request,
                                                             UriComponentsBuilder uriBuilder) {
        var question = questionService.createQuestion(request);
        var uri = uriBuilder.path("/questions/{id}").buildAndExpand(question.getId()).toUri();
        return ResponseEntity.created(uri).body(question);
    }

    @PutMapping("/{id}")
    public QuestionResponseDto updateQuestion(@PathVariable UUID id,
                                              @Valid @RequestBody QuestionUpdateDto request) {
        return questionService.updateQuestion(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
