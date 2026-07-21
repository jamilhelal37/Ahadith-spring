package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.features.interaction.entity.Comment;

import com.jamil.ahadith.features.interaction.dto.request.CommentRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.CommentResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.update.CommentUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.interaction.service.CommentService;
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
public class CommentController {
    private final CommentService commentService;
    private final AdminPageService adminPageService;

    @GetMapping({"/me/comments", "/api/v1/me/comments"})
    public List<CommentResponseDto> getMyComments() {
        return commentService.getCurrentUserComments();
    }

    @GetMapping({"/scholar/comments", "/admin/comments", "/api/v1/scholar/comments", "/api/v1/admin/comments"})
    public SearchResponse<CommentResponseDto> getComments(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(required = false) String sort) {
        return commentService.getComments(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping({"/me/comments/{id}", "/api/v1/me/comments/{id}"})
    public CommentResponseDto getMyCommentById(@PathVariable UUID id) {
        return commentService.getCurrentUserCommentById(id);
    }

    @GetMapping({"/scholar/comments/{id}", "/admin/comments/{id}", "/api/v1/scholar/comments/{id}", "/api/v1/admin/comments/{id}"})
    public CommentResponseDto getCommentById(@PathVariable UUID id) {
        return commentService.getCommentById(id);
    }

    @PostMapping({"/me/comments", "/scholar/comments", "/api/v1/me/comments", "/api/v1/scholar/comments"})
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto request,
                                                           UriComponentsBuilder uriBuilder) {
        var comment = commentService.createComment(request);
        var uri = uriBuilder.path("/api/v1/me/comments/{id}").buildAndExpand(comment.getId()).toUri();
        return ResponseEntity.created(uri).body(comment);
    }

    @PostMapping({"/me/hadiths/{hadithId}/comments", "/scholar/hadiths/{hadithId}/comments", "/api/v1/me/hadiths/{hadithId}/comments", "/api/v1/scholar/hadiths/{hadithId}/comments"})
    public ResponseEntity<CommentResponseDto> createHadithComment(@PathVariable UUID hadithId,
                                                                  @Valid @RequestBody CommentRequestDto request,
                                                                  UriComponentsBuilder uriBuilder) {
        request.setHadithId(hadithId);
        var comment = commentService.createComment(request);
        var uri = uriBuilder.path("/api/v1/me/comments/{id}").buildAndExpand(comment.getId()).toUri();
        return ResponseEntity.created(uri).body(comment);
    }

    @RequestMapping(
            value = {"/me/comments/{id}", "/scholar/comments/{id}", "/api/v1/me/comments/{id}", "/api/v1/scholar/comments/{id}"},
            method = {RequestMethod.PUT, RequestMethod.PATCH})
    public CommentResponseDto updateComment(@PathVariable UUID id,
                                           @Valid @RequestBody CommentUpdateDto request) {
        return commentService.updateCurrentUserComment(id, request);
    }

    @DeleteMapping({"/me/comments/{id}", "/scholar/comments/{id}", "/api/v1/me/comments/{id}", "/api/v1/scholar/comments/{id}"})
    public ResponseEntity<Void> deleteMyComment(@PathVariable UUID id) {
        commentService.deleteCurrentUserComment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/admin/comments/{id}", "/api/v1/admin/comments/{id}"})
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
