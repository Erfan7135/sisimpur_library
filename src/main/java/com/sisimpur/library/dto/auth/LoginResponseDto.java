package com.sisimpur.library.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String role;
}
