package com.sisimpur.library.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEditRequestDto {
    @Size(min = 1, message = "Name cannot be empty.")
    private String name;
    @Email(message = "Email should be valid")
    private String email;
    @Size(min = 5, max = 20, message = "Password should be between 5 and 20 characters")
    private String password;
    private String passwordHash;

    public UserEditRequestDto(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        if (password != null) {
        this.passwordHash = java.util.Base64.getEncoder().encodeToString(password.getBytes());
        } else {
            this.passwordHash = null;
        }
    }
}
