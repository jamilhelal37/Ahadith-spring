package com.jamil.ahadith.features.interaction.service;

import com.jamil.ahadith.core.exception.ForbiddenException;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.interaction.dto.request.CommentTextRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.AdminCommentResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.PublicCommentResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.ScholarCommentResponseDto;
import com.jamil.ahadith.features.interaction.entity.Comment;
import com.jamil.ahadith.features.interaction.exception.CommentNotFoundException;
import com.jamil.ahadith.features.interaction.mapper.CommentMapper;
import com.jamil.ahadith.features.interaction.repository.CommentRepository;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public SearchResponse<PublicCommentResponseDto> getPublicHadithComments(UUID hadithId, Pageable pageable) {
        if (!hadithRepository.existsById(hadithId)) {
            throw new HadithNotFoundException();
        }
        return adminPageService.response(commentRepository.findByHadithId(hadithId, pageable)
                .map(commentMapper::toPublicResponseDto));
    }

    public ScholarCommentResponseDto createScholarComment(UUID hadithId, CommentTextRequestDto request) {
        User user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.scholar) {
            throw new ForbiddenException("Scholar account required");
        }
        var hadith = hadithRepository.findById(hadithId).orElseThrow(HadithNotFoundException::new);
        
        Comment comment = commentMapper.toEntity(request);
        comment.setText(request.getText().strip());
        comment.setUser(user);
        comment.setHadith(hadith);
        
        comment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(comment);
        return commentMapper.toScholarResponseDto(comment);
    }

    @Transactional(readOnly = true)
    public SearchResponse<ScholarCommentResponseDto> getCurrentScholarComments(Pageable pageable) {
        User user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.scholar) {
            throw new ForbiddenException("Scholar account required");
        }
        return adminPageService.response(commentRepository.findByUserId(user.getId(), pageable)
                .map(commentMapper::toScholarResponseDto));
    }

    public ScholarCommentResponseDto updateCurrentScholarComment(UUID commentId, CommentTextRequestDto request) {
        User user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.scholar) {
            throw new ForbiddenException("Scholar account required");
        }
        var comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(CommentNotFoundException::new);
        
        comment.setText(request.getText().strip());
        comment = commentRepository.saveAndFlush(comment);
        entityManager.refresh(comment);
        return commentMapper.toScholarResponseDto(comment);
    }

    public void deleteCurrentScholarComment(UUID commentId) {
        User user = currentUserService.requireCurrentUser();
        if (user.getType() != UserType.scholar) {
            throw new ForbiddenException("Scholar account required");
        }
        var comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(CommentNotFoundException::new);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public SearchResponse<AdminCommentResponseDto> getAdminComments(Pageable pageable) {
        return adminPageService.response(commentRepository.findAll(pageable)
                .map(commentMapper::toAdminResponseDto));
    }

    @Transactional(readOnly = true)
    public AdminCommentResponseDto getAdminCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .map(commentMapper::toAdminResponseDto)
                .orElseThrow(CommentNotFoundException::new);
    }

    public void deleteAdminComment(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }
        commentRepository.deleteById(commentId);
    }
}
