package com.sisimpur.library.dto.book;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookFilterRequestDto {
    private String title;
    private String authorName;
    private String genre;
    private Long publishedYear;
    private Boolean available;
}
