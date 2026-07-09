package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.requests.BookRequestDto;
import com.jamil.ahadith.dtos.responses.BookResponseDto;
import com.jamil.ahadith.dtos.updates.BookUpdateDto;
import com.jamil.ahadith.services.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public List<BookResponseDto> getBooks() {
        return bookService.getBooks();
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookRequestDto request,
                                                     UriComponentsBuilder uriBuilder) {
        var book = bookService.createBook(request);
        var uri = uriBuilder.path("/admin/books/{id}").buildAndExpand(book.getId()).toUri();
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
