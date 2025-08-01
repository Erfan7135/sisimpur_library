package com.sisimpur.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sisimpur.library.repository.AuthorRepository;
import com.sisimpur.library.dto.author.AuthorResponseDto;
import com.sisimpur.library.dto.author.AuthorCreateRequestDto;
import com.sisimpur.library.dto.author.AuthorUpdateRequestDto;
import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.model.Author;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);
    private final AuthorRepository authorRepository;

    public AuthorResponseDto getAuthor(Long id) {
        logger.info("Fetching author with id: {}", id);
        Author author = authorRepository.findAuthorById(id)
                .orElseThrow(() -> {
                    logger.error("Author not found with id: {}", id);
                    throw new ResourceNotFoundException("Author not found");
                });
        return new AuthorResponseDto(author.getId(), author.getName(), author.getBio());
    }
        

    public AuthorResponseDto createAuthor(AuthorCreateRequestDto authorCreateRequestDto) {
        Author author = new Author();
        author.setName(authorCreateRequestDto.getName());
        author.setBio(authorCreateRequestDto.getBio());

        logger.info("Creating new author: {}", author.getName());

        Author savedAuthor = authorRepository.save(author);
        logger.info("Author created successfully: {}", savedAuthor);
        return new AuthorResponseDto(savedAuthor.getId(), savedAuthor.getName(), savedAuthor.getBio());
    }

    public AuthorResponseDto updateAuthor(AuthorUpdateRequestDto authorUpdateRequestDto, Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        if(authorUpdateRequestDto.getName() != null)
            author.setName(authorUpdateRequestDto.getName());
        if(authorUpdateRequestDto.getBio() != null)
            author.setBio(authorUpdateRequestDto.getBio());

        logger.info("Updating author with id: {}", id);
        Author updatedAuthor = authorRepository.save(author);
        logger.info("Author updated successfully: {}", updatedAuthor.getName());
        return new AuthorResponseDto(updatedAuthor.getId(), updatedAuthor.getName(), updatedAuthor.getBio());
    }

    public void deleteAuthor(Long id) {
        logger.info("Deleting author with id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        if(!author.getBooks().isEmpty()) {
            logger.error("Cannot delete author with id: {} as they have associated books", id);
            throw new IllegalStateException("Cannot delete author with associated books");
        }
        authorRepository.delete(author);
        logger.info("Author deleted successfully: {}", author.getName());
    }

    public Page<AuthorResponseDto> getAllAuthors(Pageable pageable) {
        logger.info("Fetching all authors with pagination: {}", pageable);
        Page<Author> authorsPage = authorRepository.findAll(pageable);
        return authorsPage.map(author -> new AuthorResponseDto(author.getId(), author.getName(), author.getBio()));
    }


}
