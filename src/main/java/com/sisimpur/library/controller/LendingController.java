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

}
