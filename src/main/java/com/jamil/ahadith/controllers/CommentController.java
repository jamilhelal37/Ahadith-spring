package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.CommentRequestDto;
import com.jamil.ahadith.dtos.responses.CommentResponseDto;
import com.jamil.ahadith.dtos.updates.CommentUpdateDto;
import com.jamil.ahadith.services.CommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentResponseDto> getComments() {
        return commentService.getComments();
    }

    @GetMapping("/{id}")
    public CommentResponseDto getCommentById(@PathVariable UUID id) {
        return commentService.getCommentById(id);
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto request,
                                                           UriComponentsBuilder uriBuilder) {
        var comment = commentService.createComment(request);
        var uri = uriBuilder.path("/comments/{id}").buildAndExpand(comment.getId()).toUri();
        return ResponseEntity.created(uri).body(comment);
    }

    @PutMapping("/{id}")
    public CommentResponseDto updateComment(@PathVariable UUID id,
                                           @Valid @RequestBody CommentUpdateDto request) {
        return commentService.updateComment(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}