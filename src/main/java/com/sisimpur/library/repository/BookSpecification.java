package com.sisimpur.library.repository;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import com.sisimpur.library.model.Book;
import com.sisimpur.library.model.Author;

import com.sisimpur.library.dto.book.BookFilterRequestDto;

public class BookSpecification {

    public static Specification<Book> titleContains(String title) {
        return (root, query, cb) -> {
            return title == null ? null 
            : cb.like(cb.lower(root.get("title")), "%" + 
            title.toLowerCase() + "%");
        };
    }

    public static Specification<Book> authorNameContains(String authorName) {
        return (root, query, cb) -> {
            if(authorName == null) return null;
            Join<Book, Author> authorJoin = root.join("author");
            return cb.like(cb.lower(authorJoin.get("name")), "%" + 
            authorName.toLowerCase() + "%");
        };
    }

    public static Specification<Book> genreContains(String genre) {
        return (root, query, cb) -> {
            return genre == null ? null 
            : cb.like(cb.lower(root.get("genre")), "%" + 
            genre.toLowerCase() + "%");
        };
    }

    public static Specification<Book> publishedInYear(Long year) {
        return (root, query, cb) -> {
            return year == null ? null 
            : cb.equal(root.get("publishedYear"), year);
        };
    }

    public static Specification<Book> isAvailable(){
        return (root, query, cb) -> {
            return cb.greaterThan(root.get("inStock"), 0);
        };
    }

    public static Specification<Book> getBooksByFilters(BookFilterRequestDto filter) {
        return Specification.where(titleContains(filter.getTitle()))
            .and(authorNameContains(filter.getAuthorName()))
            .and(genreContains(filter.getGenre()))
            .and(publishedInYear(filter.getPublishedYear()))
            .and(filter.getAvailable() != null && filter.getAvailable() ? isAvailable() : Specification.where(null));
    }
}
