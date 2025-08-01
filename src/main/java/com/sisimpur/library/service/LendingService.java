package com.sisimpur.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisimpur.library.model.Book;
import com.sisimpur.library.model.Lending;
import com.sisimpur.library.model.User;
import com.sisimpur.library.repository.LendingRepository;
import com.sisimpur.library.exception.ResourceNotFoundException;
import com.sisimpur.library.exception.BookNotAvailableException;
import com.sisimpur.library.exception.InvalidUserRoleException;
import com.sisimpur.library.exception.BookAlreadyBorrowedException;
import com.sisimpur.library.exception.BookAlreadyReturnedException;
import com.sisimpur.library.exception.NoActiveLendingException;

import com.sisimpur.library.dto.lending.LendingRequestDto;
import com.sisimpur.library.dto.lending.LendingResponseDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
            throw new BookNotAvailableException("Book '" + book.getTitle() + "' is currently out of stock");
        }

        // Validate user exists
        User user = userService.getUserEntityById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (user.isAdmin()) {
            throw new InvalidUserRoleException("Admin users are not allowed to borrow books");
        }

        // Check if the user already has an active lending for this book
        Optional<Lending> existingLending = lendingRepository.findByBookIdAndUserIdAndReturnDateIsNull(bookId, userId);
        if (existingLending.isPresent()) {
            throw new BookAlreadyBorrowedException("User '" + user.getName() + "' already has an active lending for book '" + book.getTitle() + "'");
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
            throw new NoActiveLendingException("No active lending found for book ID " + bookId + " and user ID " + userId);
        }

        Lending lending = lendingOpt.get();
        lending.setReturnDate(LocalDateTime.now());

        // Save the return and increase stock
        lendingRepository.save(lending);
        bookService.increaseStock(bookId);
    }

    public Page<LendingResponseDto> getAllLendings(Pageable pageable) {
        Page<Lending> lendingsPage = lendingRepository.findAll(pageable);
        return lendingsPage.map(this::mapToLendingResponseDto);
    }

    public Page<LendingResponseDto> getActiveLendings(Pageable pageable) {
        Page<Lending> lendingsPage = lendingRepository.findByReturnDateIsNull(pageable);
        return lendingsPage.map(this::mapToLendingResponseDto);
    }

    public Page<LendingResponseDto> getReturnedLendings(Pageable pageable) {
        Page<Lending> lendingsPage = lendingRepository.findByReturnDateIsNotNull(pageable);
        return lendingsPage.map(this::mapToLendingResponseDto);
    }

    @Transactional
    public LendingResponseDto returnBookById(Long lendingId) {
        Lending lending = lendingRepository.findById(lendingId)
            .orElseThrow(() -> new ResourceNotFoundException("Lending record not found with id: " + lendingId));
        
        if (lending.getReturnDate() != null) {
            throw new BookAlreadyReturnedException("Book '" + lending.getBook().getTitle() + "' has already been returned on " + lending.getReturnDate());
        }

        lending.setReturnDate(LocalDateTime.now());
        Lending savedLending = lendingRepository.save(lending);
        bookService.increaseStock(lending.getBook().getId());
        
        return mapToLendingResponseDto(savedLending);
    }

    private LendingResponseDto mapToLendingResponseDto(Lending lending) {
        return new LendingResponseDto(
            lending.getId(),
            lending.getBook().getId(),
            lending.getBook().getTitle(),
            lending.getUser().getId(),
            lending.getUser().getName(),
            lending.getLendingDate(),
            lending.getReturnDate()
        );
    }

}

