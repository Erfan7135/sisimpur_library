package com.sisimpur.library.dto.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequestDto {
    @NotBlank(message = "Title is mandatory")
    private String title;
    @Min(value = 1, message = "Author ID must be greater than 0")
    private Long authorId;
    @Min(value = 1000, message = "Published Year must be a valid year")
    @Max(value = 2025, message = "Published Year must be a valid year")
    private int publishedYear;
    private String genre;
    @Min(value = 0, message = "In Stock must be at least 0")
    private int inStock;

}