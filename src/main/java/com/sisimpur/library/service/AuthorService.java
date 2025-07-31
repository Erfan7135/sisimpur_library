package com.sisimpur.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sisimpur.library.dto.author.AuthorResponseDto;
import com.sisimpur.library.repository.AuthorRepository;
import com.sisimpur.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);
    private final AuthorRepository authorRepository;

    public AuthorResponseDto getAuthor(Long id) {
        logger.info("Fetching author with id: {}", id);
        return authorRepository.findAuthorById(id)
                    .orElseThrow(() -> {
                        logger.error("Author not found with id: {}", id);
                        throw new ResourceNotFoundException("Author not found");});
    }
}
