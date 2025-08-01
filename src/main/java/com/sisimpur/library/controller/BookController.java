package com.sisimpur.library.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;


import com.sisimpur.library.service.BookService;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.dto.book.BookCreateRequestDto;
import com.sisimpur.library.dto.book.BookUpdateRequestDto;
import com.sisimpur.library.dto.book.BookFilterRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;


@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookWithAuthor(@PathVariable @Min(1) Long id) {
        return new ResponseEntity<>(
            bookService.getBookWithAuthor(id), 
            HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDto>> getAllBooks(
        @Valid BookFilterRequestDto filter,
        @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<BookResponseDto> booksPage = bookService.getAllBooks(filter, pageable);
        return new ResponseEntity<>(
            booksPage,
            HttpStatus.OK
        );
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDto> createBook(
        @Valid @RequestBody BookCreateRequestDto bookCreateRequestDto) {
        return new ResponseEntity<>(
            bookService.createBook(bookCreateRequestDto), 
            HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDto> updateBook(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody BookUpdateRequestDto bookUpdateRequestDto) {
        return new ResponseEntity<>(
            bookService.updateBook(id, bookUpdateRequestDto),
            HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable @Min(1) Long id) {
        bookService.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
