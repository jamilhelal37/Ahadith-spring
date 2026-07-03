package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.CommentRequestDto;
import com.jamil.ahadith.dtos.responses.CommentResponseDto;
import com.jamil.ahadith.dtos.updates.CommentUpdateDto;
import com.jamil.ahadith.exceptions.CommentNotFoundException;
import com.jamil.ahadith.mappers.CommentMapper;
import com.jamil.ahadith.repositories.CommentRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;

    public List<CommentResponseDto> getComments() {
        return commentRepository.findAll().stream()
                .map(commentMapper::toResponseDto)
                .toList();
    }

    public CommentResponseDto getCommentById(UUID id) {
        return commentRepository.findById(id)
                .map(commentMapper::toResponseDto)
                .orElseThrow(CommentNotFoundException::new);
    }

    public CommentResponseDto createComment(CommentRequestDto request) {
        var comment = commentRepository.saveAndFlush(commentMapper.toEntity(request));
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

    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException();
        }
        commentRepository.deleteById(id);
    }
}