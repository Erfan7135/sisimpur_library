package com.sisimpur.library.dto.book;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateRequestDto {
    private String title;
    private Long authorId;
    private String genre;
    private int publishedYear;
    private int inStock;
}
