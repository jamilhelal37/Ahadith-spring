package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.BookRequestDto;
import com.jamil.ahadith.dtos.responses.BookResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.BookUpdateDto;
import com.jamil.ahadith.exceptions.BookNotFoundException;
import com.jamil.ahadith.mappers.BookMapper;
import com.jamil.ahadith.repositories.BookRepository;
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
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;

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
        currentUserService.getCurrentUser().ifPresent(book::setCreatedBy);
        book = bookRepository.saveAndFlush(book);
        entityManager.refresh(book);
        return bookMapper.toResponseDto(book);
    }

    public BookResponseDto updateBook(UUID id, BookUpdateDto request) {
        var book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        bookMapper.updateEntity(request, book);
        currentUserService.getCurrentUser().ifPresent(book::setUpdatedBy);
        var savedBook = bookRepository.saveAndFlush(book);
        entityManager.refresh(savedBook);
        return bookMapper.toResponseDto(savedBook);
    }

    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException();
        }
        bookRepository.deleteById(id);
    }
}
