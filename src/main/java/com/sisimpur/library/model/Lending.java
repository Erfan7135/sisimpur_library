package com.sisimpur.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import jakarta.persistence.FetchType;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "lendings")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lending_id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "lending_date", nullable = false)
    private LocalDateTime lendingDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    public Lending(User user, Book book) {
        this.user = user;
        this.book = book;
        this.lendingDate = LocalDateTime.now();
    }

}
