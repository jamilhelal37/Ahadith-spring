package com.jamil.ahadith.features.interaction.controller;

import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.request.CommentTextRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.AdminCommentResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.PublicCommentResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.ScholarCommentResponseDto;
import com.jamil.ahadith.features.interaction.service.CommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final AdminPageService adminPageService;

    // --- Public ---

    @GetMapping("/ahadith/{hadithId}/comments")
    public SearchResponse<PublicCommentResponseDto> getPublicHadithComments(
            @PathVariable UUID hadithId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return commentService.getPublicHadithComments(hadithId, adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    // --- Scholar ---

    @PostMapping("/scholar/hadiths/{hadithId}/comments")
    @PreAuthorize("hasAuthority('ROLE_SCHOLAR')")
    public ResponseEntity<ScholarCommentResponseDto> createScholarComment(
            @PathVariable UUID hadithId,
            @Valid @RequestBody CommentTextRequestDto request) {
        var comment = commentService.createScholarComment(hadithId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/scholar/comments")
    @PreAuthorize("hasAuthority('ROLE_SCHOLAR')")
    public SearchResponse<ScholarCommentResponseDto> getCurrentScholarComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return commentService.getCurrentScholarComments(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @PutMapping("/scholar/comments/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_SCHOLAR')")
    public ScholarCommentResponseDto updateCurrentScholarComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentTextRequestDto request) {
        return commentService.updateCurrentScholarComment(commentId, request);
    }

    @DeleteMapping("/scholar/comments/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_SCHOLAR')")
    public ResponseEntity<Void> deleteCurrentScholarComment(@PathVariable UUID commentId) {
        commentService.deleteCurrentScholarComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // --- Admin ---

    @GetMapping("/admin/comments")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public SearchResponse<AdminCommentResponseDto> getAdminComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return commentService.getAdminComments(adminPageService.pageable(page, size, sort,
                Set.of("createdAt", "id"),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
    }

    @GetMapping("/admin/comments/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AdminCommentResponseDto getAdminCommentById(@PathVariable UUID commentId) {
        return commentService.getAdminCommentById(commentId);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAdminComment(@PathVariable UUID commentId) {
        commentService.deleteAdminComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
