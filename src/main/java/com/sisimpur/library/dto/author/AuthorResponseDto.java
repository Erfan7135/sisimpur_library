package com.sisimpur.library.dto.author;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorResponseDto {

    private Long id;
    private String name;
    private String bio;

    public AuthorResponseDto(Long id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
    }


}

