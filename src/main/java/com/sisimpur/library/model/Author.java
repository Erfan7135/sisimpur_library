package com.sisimpur.library.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import jakarta.persistence.CascadeType;
import java.util.HashSet;

@Setter
@Getter
@Entity
@Table(name = "authors")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Author {

    @Id
    @Column(name = "author_id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "bio", length = 1000)
    private String bio;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Book> books = new HashSet<>();
}
