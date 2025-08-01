package com.sisimpur.library.dto.author;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthorUpdateRequestDto {

    @Size(min = 1, message = "Name cannot be empty.")
    private String name;

    @Size(max = 500, message = "Bio should not exceed 500 characters.")
    private String bio;
    
}
