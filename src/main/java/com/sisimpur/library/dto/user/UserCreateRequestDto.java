package com.sisimpur.library.dto.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Getter
@Setter
public class UserCreateRequestDto {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name should not exceed 50 characters")
    private String name;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 50, message = "Email should not exceed 50 characters")
    private String email;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 5, max = 20, message = "Password should be between 5 and 20 characters")
    private String password;
    private String password_hash;

    public UserCreateRequestDto(String name, String email, String password) {
        
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_hash =  java.util.Base64.getEncoder().encodeToString(password.getBytes());
    }
}
