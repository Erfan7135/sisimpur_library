package com.sisimpur.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sisimpur.library.model.Book;
import com.sisimpur.library.model.Author;

import com.sisimpur.library.repository.BookRepository;
import com.sisimpur.library.repository.AuthorRepository;

import com.sisimpur.library.exception.ResourceNotFoundException;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.dto.book.BookUpdateRequestDto;
import com.sisimpur.library.dto.book.BookCreateRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookResponseDto getBookWithAuthor(Long bookId) {
        logger.info("Fetching book with id: {}", bookId);
        return bookRepository.findBookWithAuthorById(bookId)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", bookId);
                    return new ResourceNotFoundException("Book not found with id: " + bookId);
                });
    }

    public BookResponseDto createBook(BookCreateRequestDto bookCreateRequestDto) {
        logger.info("Creating book with title: {}", bookCreateRequestDto.getTitle());
        Author author = authorRepository.findAuthorById(bookCreateRequestDto.getAuthorId())
                .orElseThrow(() -> {
                    logger.error("Author not found with id: {}", bookCreateRequestDto.getAuthorId());
                    return new ResourceNotFoundException("Author not found with id: " + bookCreateRequestDto.getAuthorId());
                });
        Book book = new Book();
        book.setTitle(bookCreateRequestDto.getTitle());
        book.setAuthor(author);
        book.setPublishedYear(bookCreateRequestDto.getPublishedYear());
        book.setGenre(bookCreateRequestDto.getGenre());
        book.setInStock(bookCreateRequestDto.getInStock());
        book = bookRepository.save(book);
        logger.info("Book created with id: {}", book.getId());
        return new BookResponseDto(book,author);
    }

    public BookResponseDto updateBook(Long bookId, BookUpdateRequestDto bookUpdateRequestDto) {
        logger.info("Updating book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        if (bookUpdateRequestDto.getAuthorId() != null) {
            Author author = authorRepository.findAuthorById(bookUpdateRequestDto.getAuthorId())
                    .orElseThrow(() -> {
                        logger.error("Author not found with id: {}", bookUpdateRequestDto.getAuthorId());
                        return new ResourceNotFoundException("Author not found with id: " + bookUpdateRequestDto.getAuthorId());
                    });
            book.setAuthor(author);
        }

        if(bookUpdateRequestDto.getTitle() != null)
            book.setTitle(bookUpdateRequestDto.getTitle());

        if(bookUpdateRequestDto.getGenre() != null)
            book.setGenre(bookUpdateRequestDto.getGenre());

        if(bookUpdateRequestDto.getPublishedYear() > 1000 && bookUpdateRequestDto.getPublishedYear() <= 2025)
            book.setPublishedYear(bookUpdateRequestDto.getPublishedYear());

        if(bookUpdateRequestDto.getInStock() >= 0)
            book.setInStock(bookUpdateRequestDto.getInStock());

        book = bookRepository.save(book);
        logger.info("Book updated with id: {}", book.getId());
        return new BookResponseDto(book, book.getAuthor());
        
    }

    public void deleteBook(Long bookId) {
        logger.info("Deleting book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        bookRepository.delete(book);
        logger.info("Book deleted with id: {}", book.getId());
    }
}
