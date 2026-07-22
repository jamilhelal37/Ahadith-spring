package com.jamil.ahadith.features.catalog.controller;

import com.jamil.ahadith.features.catalog.dto.request.BookRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.BookResponseDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.catalog.dto.update.BookUpdateDto;
import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.features.catalog.service.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping({"/admin/books", "/api/v1/admin/books"})
public class BookController {
    private final BookService bookService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<BookResponseDto> getBooks(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(required = false) String sort) {
        return bookService.getBooks(adminPageService.pageable(page, size, sort,
                Set.of("name", "createdAt", "updatedAt", "id"),
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookRequestDto request,
                                                     UriComponentsBuilder uriBuilder) {
        var book = bookService.createBook(request);
        var uri = uriBuilder.path("/api/v1/admin/books/{id}").buildAndExpand(book.getId()).toUri();
        return ResponseEntity.created(uri).body(book);
    }

    @PutMapping("/{id}")
    public BookResponseDto updateBook(@PathVariable UUID id,
                                     @Valid @RequestBody BookUpdateDto request) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
