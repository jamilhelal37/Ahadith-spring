package com.jamil.ahadith.features.catalog.controller;

import com.jamil.ahadith.features.catalog.entity.Topic;

import com.jamil.ahadith.features.catalog.dto.request.TopicRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.TopicResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.TopicUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.catalog.service.TopicService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/topics", "/api/v1/admin/topics"})
public class TopicController {
    private final TopicService topicService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<TopicResponseDto> getTopics(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(required = false) String sort) {
        return topicService.getTopics(adminPageService.pageable(page, size, sort,
                Set.of("name", "createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public TopicResponseDto getTopicById(@PathVariable UUID id) {
        return topicService.getTopicById(id);
    }

    @PostMapping
    public ResponseEntity<TopicResponseDto> createTopic(@Valid @RequestBody TopicRequestDto request,
                                                       UriComponentsBuilder uriBuilder) {
        var topic = topicService.createTopic(request);
        var uri = uriBuilder.path("/admin/topics/{id}").buildAndExpand(topic.getId()).toUri();
        return ResponseEntity.created(uri).body(topic);
    }

    @PutMapping("/{id}")
    public TopicResponseDto updateTopic(@PathVariable UUID id,
                                        @Valid @RequestBody TopicUpdateDto request) {
        return topicService.updateTopic(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}
