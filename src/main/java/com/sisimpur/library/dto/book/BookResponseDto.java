package com.sisimpur.library.dto.book;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponseDto {
    private Long id;
    private String title;
    private String authorName;
    private int publishedYear;
    private String genre;
    private int inStock;
    private int lendCount;

    public BookResponseDto(
        Long id, 
        String title, 
        String authorName, 
        int publishedYear, 
        String genre, 
        int inStock, 
        int lendCount) {
        this.id = id;
        this.title = title;
        this.authorName = authorName;
        this.publishedYear = publishedYear;
        this.genre = genre;
        this.inStock = inStock;
        this.lendCount = lendCount;
    }
    
}