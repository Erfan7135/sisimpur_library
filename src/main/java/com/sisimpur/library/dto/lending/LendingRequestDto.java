package com.sisimpur.library.dto.lending;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LendingRequestDto {
    @NotNull(message = "bookID cannot be null")
    private Long bookId;

    @NotNull(message = "userID cannot be null")
    private Long userId;
}
