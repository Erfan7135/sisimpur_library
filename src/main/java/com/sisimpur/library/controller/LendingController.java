package com.sisimpur.library.controller;

import com.sisimpur.library.dto.lending.LendingRequestDto;
import com.sisimpur.library.dto.lending.LendingResponseDto;
import com.sisimpur.library.service.LendingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/lendings")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class LendingController {

    private final LendingService lendingService;

    @PostMapping("/lend")
    public ResponseEntity<LendingResponseDto> createLending(@Valid @RequestBody LendingRequestDto requestDto) {
        LendingResponseDto responseDto = lendingService.lendBook(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PostMapping("/return")
    public ResponseEntity<Void> returnLending(@Valid @RequestBody LendingRequestDto requestDto) {
        lendingService.returnBook(requestDto.getBookId(), requestDto.getUserId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<LendingResponseDto> returnBook(@PathVariable Long id) {
        LendingResponseDto response = lendingService.returnBookById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<LendingResponseDto>> getAllLendings(
            @PageableDefault(size = 10, sort = "lendingDate") Pageable pageable) {
        Page<LendingResponseDto> lendings = lendingService.getAllLendings(pageable);
        return ResponseEntity.ok(lendings);
    }
    
    @GetMapping("/active")
    public ResponseEntity<Page<LendingResponseDto>> getActiveLendings(
            @PageableDefault(size = 10, sort = "lendingDate") Pageable pageable) {
        Page<LendingResponseDto> activeLendings = lendingService.getActiveLendings(pageable);
        return ResponseEntity.ok(activeLendings);
    }
    
    @GetMapping("/returned")
    public ResponseEntity<Page<LendingResponseDto>> getReturnedLendings(
            @PageableDefault(size = 10, sort = "returnDate") Pageable pageable) {
        Page<LendingResponseDto> returnedLendings = lendingService.getReturnedLendings(pageable);
        return ResponseEntity.ok(returnedLendings);
    }

}
