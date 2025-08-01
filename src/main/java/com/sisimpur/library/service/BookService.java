package com.sisimpur.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.exception.EntityWithActiveLendingException;

import com.sisimpur.library.model.Book;
import com.sisimpur.library.model.Author;
import com.sisimpur.library.model.Lending;
import com.sisimpur.library.repository.BookRepository;
import com.sisimpur.library.repository.AuthorRepository;
import com.sisimpur.library.repository.LendingRepository;
import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.dto.book.BookUpdateRequestDto;
import com.sisimpur.library.dto.book.BookCreateRequestDto;
import com.sisimpur.library.dto.book.BookFilterRequestDto;
import com.sisimpur.library.repository.BookSpecification;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LendingRepository lendingRepository;

    public BookResponseDto getBookWithAuthor(Long bookId) {
        logger.info("Fetching book with id: {}", bookId);
        return bookRepository.findBookWithAuthorById(bookId)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", bookId);
                    return new ResourceNotFoundException("Book not found with id: " + bookId);
                });
    }

    public Book getBookEntityById(Long bookId) {
        logger.info("Fetching book entity with id: {}", bookId);
        return bookRepository.findById(bookId)
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
        Optional<Lending> lending = lendingRepository.findByBookIdAndReturnDateIsNull(bookId);
        if (lending.isPresent()) {
            throw new EntityWithActiveLendingException("Book with id: " + bookId + " has active lendings and cannot be deleted.");
        }
        bookRepository.delete(book);
        logger.info("Book deleted with id: {}", book.getId());
    }

    public Page<BookResponseDto> getAllBooks(BookFilterRequestDto filter, Pageable pageable) {
        logger.info("Fetching all books with filters: {}, pageable: {}", filter, pageable);
        Specification<Book> spec = BookSpecification.getBooksByFilters(filter);
        Page<Book> booksPage = bookRepository.findAll(spec, pageable);
        return booksPage.map(book -> new BookResponseDto(book, book.getAuthor()));
    }

    public void decreaseStock(Long bookId) {
        logger.info("Decreasing stock for book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        if (book.getInStock() <= 0) {
            throw new IllegalStateException("Book is out of stock");
        }
        
        book.setInStock(book.getInStock() - 1);
        book.setLendCount(book.getLendCount() + 1);
        bookRepository.save(book);
        logger.info("Stock decreased for book with id: {}. New stock: {}", bookId, book.getInStock());
    }

    public void increaseStock(Long bookId) {
        logger.info("Increasing stock for book with id: {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        book.setInStock(book.getInStock() + 1);
        book.setLendCount(book.getLendCount() - 1);
        if (book.getLendCount() < 0) {
            book.setLendCount(0); // Ensure lend count does not go negative
        }
        bookRepository.save(book);
        logger.info("Stock increased for book with id: {}. New stock: {}", bookId, book.getInStock());
    }
}
