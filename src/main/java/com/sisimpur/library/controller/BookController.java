package com.sisimpur.library.controller;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.service.BookService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookWithAuthor(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(bookService.getBookWithAuthor(id));
    }
}
