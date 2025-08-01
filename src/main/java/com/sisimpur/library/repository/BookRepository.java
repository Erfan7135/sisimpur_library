package com.sisimpur.library.repository;

import com.sisimpur.library.dto.book.BookResponseDto;
import com.sisimpur.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends 
    JpaRepository<Book, Long>,
    JpaSpecificationExecutor<Book>
 {

    @Query("SELECT new com.sisimpur.library.dto.book.BookResponseDto(" +
            "b.id, b.title, a.name, b.publishedYear, b.genre, b.inStock, b.lendCount) " +
            "FROM Book b JOIN b.author a " +
            "WHERE b.id = :bookId")
    Optional<BookResponseDto> findBookWithAuthorById(@Param("bookId") Long bookId);
}
