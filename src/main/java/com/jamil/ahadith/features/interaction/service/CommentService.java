package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.interaction.entity.Comment;

import com.jamil.ahadith.features.interaction.dto.request.CommentRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.CommentResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.interaction.dto.update.CommentUpdateDto;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.interaction.exception.CommentNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.interaction.mapper.CommentMapper;
import com.jamil.ahadith.features.interaction.repository.CommentRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final HadithRepository hadithRepository;
    private final AdminPageService adminPageService;

    public SearchResponse<CommentResponseDto> getComments(Pageable pageable) {
        return adminPageService.response(commentRepository.findAll(pageable).map(commentMapper::toResponseDto));
    }

    public List<CommentResponseDto> getCurrentUserComments() {
        User user = currentUserService.requireCurrentUser();
        return commentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(commentMapper::toResponseDto)
                .toList();
    }

    public CommentResponseDto getCommentById(UUID id) {
        return commentRepository.findById(id)
                .map(commentMapper::toResponseDto)
                .orElseThrow(CommentNotFoundException::new);
    }

    public CommentResponseDto getCurrentUserCommentById(UUID id) {
        User user = currentUserService.requireCurrentUser();
        return commentRepository.findByIdAndUserId(id, user.getId())
                .map(commentMapper::toResponseDto)
                .orElseThrow(CommentNotFoundException::new);
    }

    public CommentResponseDto createComment(CommentRequestDto request) {
        if (request.getHadithId() == null) {
            throw new HadithNotFoundException();
        }
        var comment = commentMapper.toEntity(request);
        comment.setUser(currentUserService.requireCurrentUser());
        comment.setHadith(hadithRepository.findById(request.getHadithId()).orElseThrow(HadithNotFoundException::new));
        comment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(comment);
        return commentMapper.toResponseDto(comment);
    }

    public CommentResponseDto updateComment(UUID id, CommentUpdateDto request) {
        var comment = commentRepository.findById(id).orElseThrow(CommentNotFoundException::new);
        commentMapper.updateEntity(request, comment);
        var savedComment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(savedComment);
        return commentMapper.toResponseDto(savedComment);
    }

    public CommentResponseDto updateCurrentUserComment(UUID id, CommentUpdateDto request) {
        User user = currentUserService.requireCurrentUser();
        var comment = commentRepository.findByIdAndUserId(id, user.getId()).orElseThrow(CommentNotFoundException::new);
        commentMapper.updateEntity(request, comment);
        var savedComment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(savedComment);
        return commentMapper.toResponseDto(savedComment);
    }

    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException();
        }
        commentRepository.deleteById(id);
    }

    public void deleteCurrentUserComment(UUID id) {
        User user = currentUserService.requireCurrentUser();
        var comment = commentRepository.findByIdAndUserId(id, user.getId()).orElseThrow(CommentNotFoundException::new);
        commentRepository.delete(comment);
    }
}
