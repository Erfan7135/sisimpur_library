package com.sisimpur.library.dto.lending;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LendingResponseDto {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public LendingResponseDto(Long id, Long bookId, String bookTitle, Long userId, String userName, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.userId = userId;
        this.userName = userName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
