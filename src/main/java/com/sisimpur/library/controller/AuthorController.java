package com.sisimpur.library.controller;

import com.sisimpur.library.service.AuthorService;
import com.sisimpur.library.dto.author.AuthorResponseDto;
import com.sisimpur.library.dto.author.AuthorCreateRequestDto;
import com.sisimpur.library.dto.author.AuthorUpdateRequestDto;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;



@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Validated
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> getAuthor(@PathVariable @Min(1) Long id) {
        return  new ResponseEntity<>(
            authorService.getAuthor(id), 
            HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Page<AuthorResponseDto>> getAllAuthors(
        @PageableDefault(size = 10, sort = "id")Pageable pageable) {
        Page<AuthorResponseDto> authorsPage = authorService.getAllAuthors(pageable);
        return new ResponseEntity<>(
            authorsPage,
            HttpStatus.OK
        );
    }

    @PostMapping("/")
    public ResponseEntity<AuthorResponseDto> createAuthor(@Valid @RequestBody AuthorCreateRequestDto authorCreateRequestDto) {
        AuthorResponseDto createdAuthor = authorService.createAuthor(authorCreateRequestDto);
        return new ResponseEntity<>(
            createdAuthor, 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorUpdateRequestDto authorUpdateRequestDto) {
        AuthorResponseDto updatedAuthor = authorService.updateAuthor(authorUpdateRequestDto, id);
        return new ResponseEntity<>(
            updatedAuthor, 
            HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable @Min(1) Long id) {
        authorService.deleteAuthor(id);
        return new ResponseEntity<>(
            HttpStatus.NO_CONTENT
        );
    }
}

