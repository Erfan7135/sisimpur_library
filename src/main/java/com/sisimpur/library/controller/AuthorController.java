package com.sisimpur.library.controller;

import com.sisimpur.library.dto.author.AuthorResponseDto;
import com.sisimpur.library.service.AuthorService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Validated
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> getAuthor(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(authorService.getAuthor(id));
    }
}

