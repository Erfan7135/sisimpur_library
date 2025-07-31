package com.sisimpur.library.dto.author;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorCreateRequestDto {
    @NotBlank(message = "Name is mandatory")
    private String name;

    private String bio;

}
    