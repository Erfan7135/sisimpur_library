package com.sisimpur.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.model.Book;
import com.sisimpur.library.model.Lending;
import com.sisimpur.library.model.User;
import com.sisimpur.library.repository.LendingRepository;
import com.sisimpur.library.exception.ResourceNotFoundException;

import com.sisimpur.library.dto.lending.LendingRequestDto;
import com.sisimpur.library.dto.lending.LendingResponseDto;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class LendingService {

    private final LendingRepository lendingRepository;
    private final BookService bookService;
    private final UserService userService;

    @Transactional
    public LendingResponseDto lendBook(LendingRequestDto lendingRequest) {
        Long bookId = lendingRequest.getBookId();
        Long userId = lendingRequest.getUserId();

        // Validate book exists and is available
        Book book = bookService.getBookEntityById(bookId);
        if (book == null) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }

        if (book.getInStock() <= 0) {
            throw new IllegalArgumentException("Book is not in stock");
        }

        // Validate user exists
        User user = userService.getUserEntityById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (user.isAdmin()) {
            throw new IllegalArgumentException("Admin users cannot borrow books");
        }

        // Check if the user already has an active lending for this book
        Optional<Lending> existingLending = lendingRepository.findByBookIdAndUserIdAndReturnDateIsNull(bookId, userId);
        if (existingLending.isPresent()) {
            throw new IllegalArgumentException("User already has an active lending for this book");
        }

        // Create lending record
        Lending lending = new Lending();
        lending.setBook(book);
        lending.setUser(user);
        lending.setLendingDate(LocalDateTime.now());

        // Save lending record and decrease stock
        lendingRepository.save(lending);
        bookService.decreaseStock(bookId);
        return new LendingResponseDto(lending.getId(), bookId, book.getTitle(), userId, user.getName(), lending.getLendingDate(), lending.getReturnDate());
    }

    @Transactional
    public void returnBook(Long bookId, Long userId) {
        // Find the active lending record
        Optional<Lending> lendingOpt = lendingRepository.findByBookIdAndUserIdAndReturnDateIsNull(bookId, userId);
        if (lendingOpt.isEmpty()) {
            throw new IllegalArgumentException("No active lending found for this book and user");
        }

        Lending lending = lendingOpt.get();
        lending.setReturnDate(LocalDateTime.now());

        // Save the return and increase stock
        lendingRepository.save(lending);
        bookService.increaseStock(bookId);
    }

}

