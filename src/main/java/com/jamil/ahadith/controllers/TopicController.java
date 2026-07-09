package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.TopicRequestDto;
import com.jamil.ahadith.dtos.responses.TopicResponseDto;
import com.jamil.ahadith.dtos.updates.TopicUpdateDto;
import com.jamil.ahadith.services.TopicService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/topics")
public class TopicController {
    private final TopicService topicService;

    @GetMapping
    public List<TopicResponseDto> getTopics() {
        return topicService.getTopics();
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
