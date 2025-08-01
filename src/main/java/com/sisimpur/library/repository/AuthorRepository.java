package com.sisimpur.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

import com.sisimpur.library.model.Author;
import com.sisimpur.library.dto.author.AuthorResponseDto;


public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findAuthorById(Long id);

    @Query("SELECT new com.sisimpur.library.dto.author."+
            "AuthorResponseDto(a.id, a.name, a.bio) " +
            "FROM Author a ")
    Page<AuthorResponseDto> findAllAuthors(Pageable pageable);
}
