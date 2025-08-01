package com.sisimpur.library.repository;

import com.sisimpur.library.model.Lending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    Optional<Lending> findByBookIdAndUserId(Long bookId, Long userId);

    Optional<Lending> findByBookId(Long bookId);

    Optional<Lending> findByBookIdAndReturnDateIsNull(Long bookId);

    Optional<Lending> findByUserId(Long userId);
    
    Optional<Lending> findByUserIdAndReturnDateIsNull(Long userId);

    Optional<Lending> findByBookIdAndUserIdAndReturnDateIsNull(Long bookId, Long userId);
    
    Page<Lending> findByReturnDateIsNull(Pageable pageable);
    
    Page<Lending> findByReturnDateIsNotNull(Pageable pageable);
}