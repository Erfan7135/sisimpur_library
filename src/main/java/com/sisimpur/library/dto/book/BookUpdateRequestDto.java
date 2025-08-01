package com.sisimpur.library.dto.book;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class BookUpdateRequestDto {
    @Size(min = 1, message = "Title cannot be empty.")
    private String title;
    @Positive(message = "Author ID must be positive")
    private Long authorId;
    @Min(value = 1000, message = "Published Year must be a valid year")
    @Max(value = 2025, message = "Published Year must be a valid year")
    private int publishedYear;
    @Size(max = 100, message = "Genre should not exceed 100 characters.")
    private String genre;
    @Min(value = 0, message = "In Stock must be at least 0")
    private int inStock;
}
