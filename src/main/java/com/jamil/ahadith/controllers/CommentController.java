package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.CommentRequestDto;
import com.jamil.ahadith.dtos.responses.CommentResponseDto;
import com.jamil.ahadith.dtos.updates.CommentUpdateDto;
import com.jamil.ahadith.entities.Hadith;
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
public class CommentController {
    private final CommentService commentService;

    @GetMapping({"/me/comments", "/scholar/comments", "/admin/comments"})
    public List<CommentResponseDto> getComments() {
        return commentService.getComments();
    }

    @GetMapping({"/me/comments/{id}", "/scholar/comments/{id}", "/admin/comments/{id}"})
    public CommentResponseDto getCommentById(@PathVariable UUID id) {
        return commentService.getCommentById(id);
    }

    @PostMapping({"/me/comments", "/scholar/comments"})
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto request,
                                                           UriComponentsBuilder uriBuilder) {
        var comment = commentService.createComment(request);
        var uri = uriBuilder.path("/me/comments/{id}").buildAndExpand(comment.getId()).toUri();
        return ResponseEntity.created(uri).body(comment);
    }

    @PostMapping({"/me/hadiths/{hadithId}/comments", "/scholar/hadiths/{hadithId}/comments"})
    public ResponseEntity<CommentResponseDto> createHadithComment(@PathVariable UUID hadithId,
                                                                  @Valid @RequestBody CommentRequestDto request,
                                                                  UriComponentsBuilder uriBuilder) {
        Hadith hadith = new Hadith();
        hadith.setId(hadithId);
        request.setHadith(hadith);
        var comment = commentService.createComment(request);
        var uri = uriBuilder.path("/me/comments/{id}").buildAndExpand(comment.getId()).toUri();
        return ResponseEntity.created(uri).body(comment);
    }

    @RequestMapping(
            value = {"/me/comments/{id}", "/scholar/comments/{id}"},
            method = {RequestMethod.PUT, RequestMethod.PATCH})
    public CommentResponseDto updateComment(@PathVariable UUID id,
                                           @Valid @RequestBody CommentUpdateDto request) {
        return commentService.updateComment(id, request);
    }

    @DeleteMapping({"/me/comments/{id}", "/scholar/comments/{id}", "/admin/comments/{id}"})
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
