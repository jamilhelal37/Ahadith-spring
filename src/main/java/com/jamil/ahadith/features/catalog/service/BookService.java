package com.jamil.ahadith.features.catalog.service;

import com.jamil.ahadith.core.web.AdminPageService;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
import com.jamil.ahadith.features.catalog.entity.Book;

import com.jamil.ahadith.features.catalog.dto.request.BookRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.BookResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.BookUpdateDto;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;
import com.jamil.ahadith.features.catalog.mapper.BookMapper;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.search.event.ReferenceDataChangedEvent;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;
    private final MuhaddithRepository muhaddithRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditEventPublisher auditEventPublisher;

    public SearchResponse<BookResponseDto> getBooks(Pageable pageable) {
        return adminPageService.response(bookRepository.findAll(pageable).map(bookMapper::toResponseDto));
    }

    public BookResponseDto getBookById(UUID id) {
        return bookRepository.findById(id)
                .map(bookMapper::toResponseDto)
                .orElseThrow(BookNotFoundException::new);
    }

    public BookResponseDto createBook(BookRequestDto request) {
        var book = bookMapper.toEntity(request);
        book.setMuhaddith(muhaddithRepository.findById(request.getMuhaddith().getId())
                .orElseThrow(MuhaddithNotFoundException::new));
        currentUserService.getCurrentUser().ifPresent(book::setCreatedBy);
        book = bookRepository.saveAndFlush(book);
        entityManager.refresh(book);
        publishReferenceDataChanged();
        auditEventPublisher.publishCreate("books", book.getId(), AuditData.snapshot(book));
        return bookMapper.toResponseDto(book);
    }

    public BookResponseDto updateBook(UUID id, BookUpdateDto request) {
        var book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        var oldData = AuditData.snapshot(book);
        bookMapper.updateEntity(request, book);
        if (request.getMuhaddith() != null) {
            book.setMuhaddith(muhaddithRepository.findById(request.getMuhaddith().getId())
                    .orElseThrow(MuhaddithNotFoundException::new));
        }
        currentUserService.getCurrentUser().ifPresent(book::setUpdatedBy);
        var savedBook = bookRepository.saveAndFlush(book);
        entityManager.refresh(savedBook);
        publishReferenceDataChanged();
        auditEventPublisher.publishUpdate("books", savedBook.getId(), oldData, AuditData.snapshot(savedBook));
        return bookMapper.toResponseDto(savedBook);
    }

    public void deleteBook(UUID id) {
        var book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        var oldData = AuditData.snapshot(book);
        bookRepository.delete(book);
        publishReferenceDataChanged();
        auditEventPublisher.publishDelete("books", id, oldData);
    }

    private void publishReferenceDataChanged() {
        eventPublisher.publishEvent(new ReferenceDataChangedEvent());
    }
}
