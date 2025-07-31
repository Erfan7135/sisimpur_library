package com.sisimpur.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    public BookResponseDto getBookWithAuthor(Long bookId) {
        logger.info("Fetching book with id: {}", bookId);
        return bookRepository.findBookWithAuthorById(bookId)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", bookId);
                    return new ResourceNotFoundException("Book not found with id: " + bookId);
                });
    }

}
